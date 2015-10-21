package bmpinterface;

/**
 * Created by diego on 09-10-15.
 */
public class Unfilter {
    private int bpp;    //Byte of the prior pixel
    private int width;
    private byte[] data;
    public Unfilter(int width,int bpp, byte[] data){
        this.width = width;
        this.data = data;
        this.bpp = bpp;


        for (int i=0; i<data.length; i += (width+1)){
            int filterType = data[i];
            if (filterType==0){         //i.e. "None"
                continue;
            }

            else if (filterType==1){    //i.e. "Sub": sub(x) = raw(x) - raw(x-bpp)
                for (int j=1; j<=width; j++){
                    data[i+j] = (byte) (data[i+j] + rawMinusBpp(i+j));
                }
            }

            else if (filterType==2){    //i.e. "Up": up(x) = raw(x) - prior(x)
                for (int j=1; j<=width; j++){
                    data[i+j] = (byte) (data[i+j] + prior(i+j));
                }
            }

            else if (filterType==3){    //i.e. "Average": average(x) = raw(x) - floor( ( raw(x-bpp)+prior(x) )/2 )
                for (int j=1; j<=width; j++){
                    data[i+j] = (byte) (data[i+j] + (rawMinusBpp(i+j)+prior(i+j))/2);
                }
            }
            else if (filterType==4){    //i.e. "Paeth": paeth(x) = raw(x) + paethPredictor( raw(x-bpp), prior(x), prior(x-bpp) )
                for (int j=1; j<=width; j++){
                    int predictor = paethPredictor (i+j);
                    data[i+j] =  (byte)((data[i+j]+ predictor));
                }
            }

        }
    }

    public int paethPredictor(int i){
        return paethPredictor(rawMinusBpp(i), prior(i), priorMinusBpp(i));
    }

    public int paethPredictor (int a, int b, int c) {
        int p = (a + b - c);//  initial estimate
        int pa = Math.abs(p - a);//   distances to a, b, c
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        if (pa<=pb && pa<=pc)   return a;
        else if (pb<=pc)    return b;
        return c;
    }

    public int rawMinusBpp(int i){
        if (i%(width+1) <= bpp)   return 0;
        else return data[i-bpp]&0xFF;
    }

    public int prior(int i){
        if (i<=width)   return 0;
        else return data[i-(width+1)]&0xFF;
    }

    public int priorMinusBpp(int i){
        if(i%(width+1) <= bpp || i<=width+1 )   return 0;
        else {
            return data[i-(width+1)-bpp]&0xFF;
        }
    }
}
