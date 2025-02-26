//Karissa Wiggins
import java.util.Scanner;

public class BlockCipher {
    public static final int[] SUB={0xC,0x6,0x9,0x4,0xF,0x3,0xA,0x1,0xD,0xE,0x7,0x5,0x2,0x8,0x0,0xB};

    public static byte[] hexByte(String hex){
        //trim padding and whitespace
        hex=hex.trim();
        //buffer with extra zero so it will always be an even number of blocks
        if(hex.length()%2 != 0){
            hex="0"+hex;
        }
        //convert hex to byte array
        int n=hex.length();
        //2 hex digits represents 1 byte so array is half amount elements as hex string length
        byte[] result=new byte[n/2];
        //loop processes 2 characters at a time by incrementing by 2
        for(int i=0;i<n;i+=2){
            //converts hex digit into numeric value at base 16
            int upper=Character.digit(hex.charAt(i),16);
            int lower=Character.digit(hex.charAt(i+1),16);
            //combines by shifting left and adds lower bits 
            int combined=(upper*16)+lower;
            result[i/2]=(byte) combined;
        }
        return result;
    }

    //converts byte array to hex
    public static String byteHex(byte[] input){
        int n=input.length;
        //Builds string by appending and then converts result into string which is more efficient than concatenating strings
        StringBuilder build=new StringBuilder();
        for(byte i=0;i<n;i++){
            build.append(String.format("%02X",input[i]));
        }

        return build.toString();
    }

    public static int substitute(int bits){
        //get lower bits
        int index=bits%16;
        if(index<0)
            index+=16;

        return SUB[index];
    }

    public static byte[] ecbEncrypt(byte[] plaintext){
        int n=plaintext.length;
        byte[] cipher=new byte[n];
        for(int i=0;i<n;i++){
            //shift 4 bits to right and mask with 0xF so only 4 bits left to get upper bits
            int upper=(plaintext[i]>>4) & 0xF;
            int lower=plaintext[i] & 0xF;
            int ecbUpper=substitute(upper);
            int ecbLower=substitute(lower);
            
            //combine bits
            int combined=(ecbUpper*16)+ecbLower;
            cipher[i]=(byte) combined;
        }
        
        return cipher;
    }

    public static byte[] ctrEncrypt(byte[] plaintext,int iv){
        int n=plaintext.length;
        byte[] cipher=new byte[n];
        int counter=0;

        for(int i=0;i<n;i++){
            int upper=(plaintext[i]>>4) & 0xF;
            int lower=plaintext[i] & 0xF;
            //keystream by adding counter to IV
            int keyUpper=substitute((iv + counter++) % 16);
            int keyLower=substitute((iv + counter++) % 16);
            //XOR plaintext with keystream
            int ctrUpper=upper ^ keyUpper;
            int ctrLower=lower ^ keyLower;
            
            int combined=(ctrUpper*16)+ctrLower;
            cipher[i]=(byte) combined;
        }

        return cipher;
    }

    public static byte[] cbcEncrypt(byte[] plaintext,int iv){
        int n=plaintext.length;
        byte[] cipher=new byte[n];
        int newIV=iv & 0xF;

        for(int i=0;i<n;i++){
            int upper=(plaintext[i]>>4) & 0xF;
            int lower=plaintext[i] & 0xF;
            
            int cbcUpper=substitute(upper ^ newIV);
            newIV=cbcUpper;

            int cbcLower=substitute(lower ^ newIV);
            newIV=cbcLower;

            int combined=(cbcUpper*16)+cbcLower;
            cipher[i]=(byte) combined;
        }
        
        return cipher;
    }

    public static void main(String[] args) {
        Scanner scan=new Scanner(System.in);
        
        System.out.print("Enter plaintext (in hex): ");
        String plain=scan.nextLine().trim();
        
        System.out.print("Enter IV (one hex digit): ");
        String initial=scan.nextLine().trim();

        byte[] plaintext=hexByte(plain);
        //convert IV from hex to integer and only lower bits used
        int iv=Integer.parseInt(initial,16) & 0xF;

        System.out.println();
        System.out.println("Plaintext: "+byteHex(plaintext)+"\n");
        System.out.println("ECB Encrypted: "+byteHex(ecbEncrypt(plaintext)));
        System.out.println("CTR Encrypted: "+byteHex(ctrEncrypt(plaintext,iv)));
        System.out.println("CBC Encrypted: "+byteHex(cbcEncrypt(plaintext,iv)));
       
        scan.close();
    }




}
