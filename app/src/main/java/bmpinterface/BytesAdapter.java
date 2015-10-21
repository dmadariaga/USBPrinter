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

    public int length(){
        return data.length;
    }

    public byte getByteFromBits(int bitDepth, int offset, int pos){
        int mask = 0x00;
        for (int i=0; i<bitDepth; i++){
            mask = (mask | (0x80>>i));
        }
        return (byte)((getByte(offset) & (mask>>(bitDepth*pos)))>>bitDepth*(8/bitDepth-bitDepth*pos-1));

    }

    public void setByte(int i, byte b){
        data[i] = b;
    }

    public void unfilterScanline(int i, int width){
        int filterType = data[i];
        if (filterType==0){
            return;
        }
        else if (filterType==1){

        }
    }

}

