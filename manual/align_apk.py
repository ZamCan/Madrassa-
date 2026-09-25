import struct
import zlib
import zipfile
from pathlib import Path

import sys

if len(sys.argv) != 3:
    raise SystemExit("Usage: align_apk.py <input.apk> <output.apk>")

src = Path(sys.argv[1])
dst = Path(sys.argv[2])

with zipfile.ZipFile(src, "r") as zin:
    entries = zin.infolist()

    output = bytearray()
    central = []

    for info in entries:
        name = info.filename.encode("utf-8")
        raw = zin.read(info.filename)

        # classes.dex and resources.arsc must stay uncompressed
        # (resources.arsc is required to be STORED on API 30+).
        # assets/ must also stay uncompressed: AssetManager.openFd()
        # refuses compressed entries, and the Solo audio player
        # needs a real file descriptor for MediaPlayer.
        if (
            info.filename in ("classes.dex", "resources.arsc")
            or info.filename.startswith("assets/")
        ):
            method = zipfile.ZIP_STORED
            compressed = raw
        else:
            method = zipfile.ZIP_DEFLATED

            compressor = zlib.compressobj(
                level=9,
                method=zlib.DEFLATED,
                wbits=-15
            )
            compressed = (
                compressor.compress(raw) +
                compressor.flush()
            )

        crc = zlib.crc32(raw) & 0xffffffff

        # Local header is 30 bytes + filename.
        # Pad so the ACTUAL DATA begins on a 4096-byte boundary.
        header_size = 30 + len(name)
        pad = (4096 - ((len(output) + header_size) % 4096)) % 4096

        extra = b"\x00" * pad

        local_offset = len(output)

        local_header = struct.pack(
            "<IHHHHHIIIHH",
            0x04034B50,
            20,
            0,
            method,
            info.date_time[3] << 11 |
            info.date_time[4] << 5 |
            (info.date_time[5] // 2),
            0,
            crc,
            len(compressed),
            len(raw),
            len(name),
            len(extra)
        )

        output.extend(local_header)
        output.extend(name)
        output.extend(extra)

        data_offset = len(output)

        assert data_offset % 4096 == 0, (
            f"{info.filename}: data offset {data_offset} not aligned"
        )

        output.extend(compressed)

        central.append(
            (
                name,
                method,
                crc,
                len(compressed),
                len(raw),
                local_offset
            )
        )

        print(
            f"{info.filename}: "
            f"data_offset={data_offset} "
            f"aligned={data_offset % 4096 == 0}"
        )

    cd_offset = len(output)

    for name, method, crc, comp_size, raw_size, local_offset in central:

        header = struct.pack(
            "<IHHHHHHIIIHHHHHII",
            0x02014B50,
            20,
            20,
            0,
            method,
            0,
            0,
            crc,
            comp_size,
            raw_size,
            len(name),
            0,
            0,
            0,
            0,
            0,
            local_offset
        )

        output.extend(header)
        output.extend(name)

    cd_size = len(output) - cd_offset

    eocd = struct.pack(
        "<IHHHHIIH",
        0x06054B50,
        0,
        0,
        len(central),
        len(central),
        cd_size,
        cd_offset,
        0
    )

    output.extend(eocd)

dst.write_bytes(output)

print()
print(f"Created: {dst}")
print(f"Size: {dst.stat().st_size} bytes")
