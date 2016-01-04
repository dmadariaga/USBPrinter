package bmpinterface;

/**
 * Created by diego on 25-09-15.
 */
public class BytesAdapter {
    private byte[] data;
    public BytesAdapter(byte[] fileData){
        data = fileData;
    }

    public static int toInt(byte[]a, int start, int length){
        int i;
        int result = 0;
        int pow = 1;
        for(i=0; i<length; i++){
            result += (a[start+i]&0xFF)*pow;
            pow *=16*16;
        }
        return result;
    }

    public int bigEndianToInt(int start, int length){
        int i;
        int result = 0;
        int pow = 1;
        for(i=length-1; i>=0; i--){
            result += (data[start+i]&0xFF)*pow;
            pow *=16*16;
        }
        return result;
    }
{

        }
    public int find(int start, byte[]s){
        boolean f = true;
        for (int i=start; i<data.length - s.length; i++){
            for (int j=0; j<s.length; j++){
                if (data[i+j]!=s[j]){
                    f = false;
                    break;
                }
            }
            if (!f){
                f = true;
                continue;
            }
            return i;
        }
        return -1;
    }

    public byte[] getData(){
        return data;

    }

    public byte[] getData(int offset, int length){
        byte[] result = new byte[length];
        for (int i=0; i<length; i++){
            result[i] = getByte(offset + i);
        }
        return result;
    }

    public byte getByte(int i){
        return getData()[i];
    }

    public int getUnsignedByte(int i){
        return getData()[i] & 0xFF;
    }

    public int length(){
        return data.length;
    }

    public byte getByteFromBits(int bitDepth, int offset, int pos){
        int mask = 0x00;
        for (int i=0; i<bitDepth; i++){
            mask = (mask | (0x80>>i));

        }
        return (byte)((getByte(offset) & (mask>>(bitDepth*pos)))>>(8-bitDepth*(pos+1)));

    }
    public static String toHexString(byte b){

        String out = Integer.toHexString(b & 0xFF) + "0";
        if ( (b & 0xFF) < 0x10 ){
            out = "0" + out;
        }
        return out.substring(0,2);
    }

    public static String toHexString(int i){

        String out = Integer.toHexString(i) + "0";
        if ( (i) < 0x10 ){
            out = "0" + out;
        }
        return out.substring(0,2);
    }


    public void setByte(int i, byte b){
        data[i] = b;
    }
}

