package com.zamcan.madrassa.data.geography;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CountryCatalog {
    private static final String DEFAULT_COUNTRY = "TZ";
    private static final Set<String> PRIORITY = new HashSet<>();
    private static final Map<String,String> DIAL_CODES = new HashMap<>();
    static {
        Collections.addAll(PRIORITY,"TZ","KE","UG","RW","BI","ET","SO","DJ","SD","EG","SA","AE","OM","QA","KW","BH","JO","PS","IQ","SY","YE","LY","TN","DZ","MA","MR","KM");
        String data="TZ=+255;KE=+254;UG=+256;RW=+250;BI=+257;ET=+251;SO=+252;DJ=+253;SD=+249;EG=+20;SA=+966;AE=+971;OM=+968;QA=+974;KW=+965;BH=+973;JO=+962;PS=+970;IQ=+964;SY=+963;YE=+967;LY=+218;TN=+216;DZ=+213;MA=+212;MR=+222;KM=+269;US=+1;CA=+1;GB=+44;IE=+353;FR=+33;DE=+49;IT=+39;ES=+34;PT=+351;NL=+31;BE=+32;CH=+41;AT=+43;SE=+46;NO=+47;DK=+45;FI=+358;PL=+48;CZ=+420;SK=+421;HU=+36;RO=+40;GR=+30;TR=+90;RU=+7;UA=+380;IN=+91;PK=+92;BD=+880;LK=+94;NP=+977;CN=+86;JP=+81;KR=+82;MY=+60;SG=+65;ID=+62;TH=+66;PH=+63;VN=+84;AU=+61;NZ=+64;ZA=+27;NG=+234;GH=+233;SN=+221;CI=+225;CM=+237;CD=+243;CG=+242;ZM=+260;ZW=+263;MW=+265;MZ=+258;NA=+264;BW=+267;LS=+266;SZ=+268;MG=+261;MU=+230;SC=+248;ER=+291;SS=+211;SL=+232;LR=+231;GM=+220;GN=+224;ML=+223;BF=+226;NE=+227;TD=+235;CF=+236;GA=+241;GQ=+240;AO=+244;CV=+238;ST=+239;BJ=+229;TG=+228;BR=+55;MX=+52;AR=+54;CL=+56;CO=+57;PE=+51;VE=+58;BO=+591;EC=+593;UY=+598;PY=+595;CR=+506;PA=+507;DO=+1;JM=+1;CU=+53;IR=+98;AF=+93;KZ=+7;UZ=+998;TM=+993;TJ=+992;KG=+996;AZ=+994;GE=+995;AM=+374;IL=+972;LB=+961;MT=+356;CY=+357;HR=+385;RS=+381;BG=+359;SI=+386;BA=+387;MK=+389;AL=+355;EE=+372;LV=+371;LT=+370;IS=+354;LU=+352;FJ=+679;PG=+675;WS=+685;TO=+676;VU=+678;SB=+677;KH=+855;LA=+856;MM=+95;MN=+976;BN=+673";
        for(String item:data.split(";")){String[] pair=item.split("=",2);if(pair.length==2)DIAL_CODES.put(pair[0],pair[1]);}
    }
    private CountryCatalog(){}
    public static String getDefaultCountryCode(){return DEFAULT_COUNTRY;}
    public static List<Country> all(){
        List<Country> out=new ArrayList<>();
        for(String code:Locale.getISOCountries()){
            Locale locale=new Locale("",code);
            out.add(new Country(code,locale.getDisplayCountry(Locale.ENGLISH),DEFAULT_COUNTRY.equals(code),PRIORITY.contains(code),"TZ".equals(code),DIAL_CODES.getOrDefault(code,"")));
        }
        return sort(out);
    }
    public static List<Country> sort(List<Country> input){
        List<Country> out=new ArrayList<>(input);
        Collections.sort(out,new Comparator<Country>(){public int compare(Country a,Country b){int ra=rank(a),rb=rank(b);return ra!=rb?Integer.compare(ra,rb):a.name.compareToIgnoreCase(b.name);}});
        return out;
    }
    public static Country findByCode(String code){if(code==null)return null;String wanted=code.trim().toUpperCase(Locale.ROOT);for(Country c:all())if(wanted.equals(c.code))return c;return null;}
    private static int rank(Country c){if(c==null||c.code==null)return 1000;if(DEFAULT_COUNTRY.equalsIgnoreCase(c.code))return 0;if("KE".equalsIgnoreCase(c.code))return 10;if("UG".equalsIgnoreCase(c.code))return 20;return PRIORITY.contains(c.code.toUpperCase(Locale.ROOT))?30:100;}
}