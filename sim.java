package com.company;
import java.io.File;
import java.math.BigInteger;
import java.util.Scanner;
import java.text.DecimalFormat;
class sim {
    static String Predictor_model;
    static int gM=0;
    static int N=0;
    static int bM=0;
    static int kc=0;
    static int cntrBits=0;
    static int smthCorrect=0;
    static char PredSM;
    static int SMpcTable=0;

    static String model = "";
    static String address="";
    static String pred="";
    static int GBHR=0;
    static int ctr=0;
    static int tkn=0;
    static int mx=0;
    // static int tk=0;
    //static int Ntk=0;
    final static int taken=1;
    final static int NTtaken=0;
    static int act;
    static int G_shrPred=0;
    static int Bimodal_Pred=0;
    static String tracefile="";
    static int bimodal_Prediction=0;
    static int gshare_Prediction=0;
    static int hybrid_Prediction=0;
    static DecimalFormat fmt = new DecimalFormat("#0.00");

    static  int numPredictions = 0;
    static int numSmithMisPredictions=0;
    static int numGshareMisPredictions = 0;
    static int numBimodalMisPredictions = 0;
    static int numHybridMisPredictions = 0;
    static int bct=0;
    static int gct=0;

    static double missPredictionRate=0;
    static int TotalMissPrediction=0;
    static int Bi_BHT[];
    static int G_BHT[];
    static int CHST[];
    int numGshareMisses = 0;
    static String argValue;
    //final static String COMMAND = "COMMAND";
    //final static String OUTPUT = "OUTPUT";
    public sim(String [] args)//constructor
    {
        model=args[0];
        switch (model) {
            case"smith":
                cntrBits=Integer.parseInt(args[1]);
                tracefile=args[2];
                ctr= (int) Math.pow(2, cntrBits-1);
                tkn= (int) Math.pow(2, cntrBits-1);
                mx= (int) Math.pow(2, cntrBits) - 1;
                argValue="./sim smith " + cntrBits + " " + tracefile;



                break;
            case "bimodal":
                bM = Integer.parseInt(args[1]);
                tracefile=args[2];
                argValue="./sim bimodal " + bM + " " + tracefile;
                break;
            case "gshare":
                gM = Integer.parseInt(args[1]);
                N = Integer.parseInt(args[2]);
                tracefile = args[3];
                argValue=  "./sim gshare " + gM + " " + N + " " + tracefile;
                break;
            case"hybrid":
                kc= Integer.parseInt(args[1]);
                gM= Integer.parseInt(args[2]);
                N = Integer.parseInt(args[3]);
                bM = Integer.parseInt(args[4]);
                tracefile = args[5];
                argValue="./sim hybrid " + kc + " " + gM + " " + N + " " + bM + " "  + tracefile;
                break;
        }
        G_BHT = new int[(int) Math.pow(2, gM)];
        Bi_BHT = new int[(int) Math.pow(2, bM)];
        CHST= new int[(int) Math.pow(2, kc)];
        initialiseTables();

    }// constructor ends here

    public static void main(String[] args) {

        sim pd= new sim(args);

        try {

            Scanner myReader = new Scanner(new File(tracefile));
            //Scanner myReader =new Scanner((myObj));
            while (myReader.hasNext()) {
                address = myReader.next().trim();

                pred = myReader.next().trim();
                if (pred.equals("t")) {
                    act = taken;
                } else if (pred.equals("n")) {
                    act = NTtaken;
                }
                numPredictions++;
                switch (model) {
                    // System.out.println("Index bits in BHR: "+GsharePred());
                    case"smith":
                        boolean actPred=false;
                        boolean preds=false;
                        if(ctr>=tkn)
                        {
                            preds=true;
                        }
                        if(act==1&& ctr<mx)
                        {
                            ctr++;
                        }
                        else if(act==0 && ctr>0)
                        {
                            ctr--;
                        }
                        if (act==1)
                        {
                            actPred=true;

                        }
                        else{actPred=false;}
                        if(preds != actPred)
                        {
                            numSmithMisPredictions++;
                        }




                        break;
                    case"bimodal":

                        Bimodal_Pred=BimodalPred();
                        boolean bPredVal=compACTAndPred(act,Bimodal_Pred);
                        missPred("bimodal",bPredVal);
                        int Bimodal_Index=findIndex(address,bM,N,"bimodal");
                        ChangePCTableVal(Bimodal_Index,act,Bi_BHT);

                        break;
                    case"gshare":

                        G_shrPred = GsharePred();
                        boolean PredVal = compACTAndPred(act, G_shrPred);//comparing the pred and actual value
                        missPred("gshare", PredVal);
                        int G_shareIndex = getIndex(address, gM);
                        ChangePCTableVal(G_shareIndex, act, G_BHT);
                        if (N != 0) {
                            updGBHR(N, act);
                        }
                        break;
                    case"hybrid":
                        int Bimodalpred=BimodalPred();
                        int GsharePred=GsharePred();
                        int chIndex=getOtIndex(address,kc);
                        int PHVaule=findTableCount(chIndex,CHST);
                        if(PHVaule>= 2)
                        {
                            //check for mispredictions
                            if (act==1  && GsharePred==0) {
                                numHybridMisPredictions++;
                            }
                            if(act==0  && GsharePred==1){
                                numHybridMisPredictions++;}
                            //update the gshare table
                            int gIndex=getIndex(address,gM);
                            ChangePCTableVal(gIndex, act, G_BHT);;
                        }
                        else
                        {
                            // check for mispredictions
                            if (act==1  && Bimodalpred==0)
                            {
                                numHybridMisPredictions++;
                            }
                            if(act==0  && Bimodalpred==1)
                            {
                                numHybridMisPredictions++;
                            }
                            //update the bimodal table and the global history table
                            int bIndex=findIndex(address,bM,N,"bimodal");
                            ChangePCTableVal(bIndex, act, Bi_BHT);
                        }
                        if (N != 0) {
                            updGBHR(N, act);
                        }
                        if((act==1  && Bimodalpred==1)||(act==0  && Bimodalpred==0))
                        {
                            bct=1;

                        }
                        else
                        {
                            bct=0;
                        }
                        if((act==1  && GsharePred==1)||(act==0  && GsharePred==0))
                        {
                            gct=1;

                        }
                        else
                        {
                            gct=0;
                        }
                        if(gct==1 && bct==0)
                        {
                            if(CHST[chIndex] < 3)
                            {
                                CHST[chIndex]++;
                            }
                        }
                        if(bct==1 && gct==0)
                        {
                            if(CHST[chIndex] > 0)
                            {
                                CHST[chIndex]--;
                            }
                        }


                        break;
                }
            }



        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        /*if(model.equals("smith"))//to get the smith prediction rate
        {
            numSmithMisPredictions=numPredictions-smthCorrect;
        }*/


        getMisPredictionRate(model);
        getOUTPUT(model);


    }
    public void initialiseTables(){


        for(int i=0; i<Bi_BHT.length; i++){
            Bi_BHT[i] = 4;
        }


        for(int i=0; i<G_BHT.length; i++){
            G_BHT[i] = 4;
        }
        for(int i=0; i<CHST.length; i++){
            CHST[i] = 1;
        }


    }




    //Required functions
    public static int BimodalPred(){
        //int bIndex=getOtIndex(address,bM);
        int bIndex=findIndex(address,bM,N,"bimodal");
        int PBValue= findTableCount(bIndex,Bi_BHT);
        int predic=findPrediction(PBValue,"bimodal");
        return predic;
    }

    public static int GsharePred(){
        String PredModel = "gshare";
        // int index = findIndex(address, M, N, PredModel);
        int index=getIndex(address,gM);
        int PValue = findTableCount(index,G_BHT);
        int prediction = findPrediction( PValue,PredModel);
        return prediction;

    }
    public static int HybridPred(){
        Bimodal_Pred=BimodalPred();
        G_shrPred = GsharePred();
        int hyd_Index=getOtIndex(address,kc);
        int PHVaule=findTableCount(hyd_Index,CHST);
        int predictHYB=findPrediction(PHVaule,"hybrid");
        return predictHYB;
    }
    public static String HexToBin(String in){
        return String.format("%24s", new BigInteger(in, 16).toString(2)).replace(' ', '0');
    }
    public static int BinStrToDec(String in){
        //System.out.println(in.length());
        return Integer.parseInt(in, 2);
    }
    public static int findIndex(String adrs,int PCz,int GBHsz,String mdlName)
    {
        String binAddress=HexToBin(adrs);
        if(mdlName.equals("bimodal"))
        {
            return BinStrToDec(binAddress.substring(binAddress.length() - (PCz+2), binAddress.length() - 2));
        }


        if(mdlName.equals("hybrid"))
        {
            return BinStrToDec(binAddress.substring(binAddress.length() - (PCz+2), binAddress.length() - 2));
        }
        return 1;
    }
    public static int getIndex(String addrs, int bt){

        int deciloc = Integer.parseInt(addrs, 16);
        deciloc = deciloc >> 2;
        StringBuilder sb = new StringBuilder();
        while(bt > 0) {
            sb.append((deciloc & 1));
            deciloc = deciloc >> 1;
            bt--;
        }
        String ab = sb.reverse().toString();
        int index = Integer.parseInt(ab, 2);
        index=index^GBHR;
        return index;
    }
    public static int getOtIndex(String addrs, int bt){

        int deciloc = Integer.parseInt(addrs, 16);
        deciloc = deciloc >> 2;
        StringBuilder sb = new StringBuilder();
        while(bt > 0) {
            sb.append((deciloc & 1));
            deciloc = deciloc >> 1;
            bt--;
        }
        String ab = sb.reverse().toString();
        int index = Integer.parseInt(ab, 2);
        return index;
    }
    public static int findTableCount( int idx,int PCtable[]){
        return PCtable[idx];
    }
    public static int findPrediction(int cnt,String mdl)
    {
        if(mdl.equals("bimodal"))
        {
            if(( cnt == 4) || ( cnt== 5) || ( cnt== 6) || ( cnt== 7)  )
            {
                return taken;
            }
            else{ // count is 0 or 1
                return NTtaken;
            }
        }
        if(mdl.equals("gshare"))
        {
            if(( cnt == 4) || ( cnt== 5) || ( cnt== 6) || ( cnt== 7)  )
            {
                return taken;
            }
            else{ // count is 0 or 1
                return NTtaken;
            }
        }
        if(mdl.equals("hybrid")) {
            if ((cnt > 1)) {
                int index = getIndex(address, gM);
                ChangePCTableVal(index, act, G_BHT);
                return G_shrPred;
            } else { // count is 0 or 1
                int index = findIndex(address, bM, N, "bimodal");
                ChangePCTableVal(index, act, Bi_BHT);
                return Bimodal_Pred;
            }
        }
        return 0;
    }
    public static boolean compACTAndPred( int actual,int pred){
        if( actual==pred){
            return true;
        }
        else{
            return false;
        }
    }
    public static void missPred(String mdl,boolean comp)
    {
        if(mdl.equals("bimodal"))
        {
            if(!comp)
            {
                numBimodalMisPredictions++;
            }
        }
        if(mdl.equals("gshare"))
        {
            if(!comp)
            {
                numGshareMisPredictions++;
            }
        }
        if(mdl.equals("hybrid"))
        {
            if(!comp)
            {
                numHybridMisPredictions++;
            }
        }
    }
    public static void ChangePCTableVal( int idx, int act,int table[]){
        if( (act == taken) && (table[idx] < 7) ){ // increment if actual outcome was taken, but saturate at 3
            table[idx]++;
        }
        if( (act == NTtaken) && (table[idx] > 0) ){ // decrement if actual outcome was not taken, but saturate at 0
            table[idx]--;
        }
    }
    public static void ChangeChooserTableVal(int idx,String model)
    {
        boolean isBiMdlCrt=  compACTAndPred(act,Bimodal_Pred);
        boolean isGshCRT= compACTAndPred(act, G_shrPred);
        if(isBiMdlCrt && !isGshCRT)
        {
            ChangePCTableVal(idx,NTtaken,CHST);
        }
        else if(!isBiMdlCrt && isGshCRT)
        {
            ChangePCTableVal(idx,taken,CHST);
        }


    }
    public static void getMisPredictionRate(String model){
        if(model.equals("smith"))
        {
            TotalMissPrediction= numSmithMisPredictions;

        }
        if(model.equals("bimodal"))
        {
            TotalMissPrediction= numBimodalMisPredictions;

        }
        if(model.equals("gshare"))
        {
            TotalMissPrediction= numGshareMisPredictions;

        }
        if(model.equals("hybrid"))
        {
            TotalMissPrediction= numHybridMisPredictions;

        }
        missPredictionRate = (double) ((float) TotalMissPrediction / (float) numPredictions)*100;
    }

    public static void getOUTPUT(String model) {
        switch(model){
            case"smith":
                System.out.println("COMMAND");
                System.out.println(argValue);
                System.out.println("OUTPUT");
                System.out.println("number of predictions: "+numPredictions);
                System.out.println("number of mispredictions: "+numSmithMisPredictions);
                System.out.println("misprediction rate: "+fmt.format(missPredictionRate) + "%");
                System.out.println("FINAL COUNTER CONTENT:          " + ctr);

                break;
            case"bimodal":
                System.out.println("COMMAND");
                System.out.println(argValue);
                System.out.println("OUTPUT");
                System.out.println("number of predictions: "+numPredictions);
                System.out.println("number of mispredictions: "+numBimodalMisPredictions);
                System.out.println("misprediction rate: "+fmt.format(missPredictionRate) + "%");
                System.out.println("FINAL BIMODAL CONTENTS");
                for(int i=0; i<Bi_BHT.length; i++){
                    System.out.println(i + "   " + Bi_BHT[i]);
                }


                break;
            case"gshare":
                System.out.println("COMMAND");
                System.out.println(argValue);
                System.out.println("OUTPUT");
                System.out.println("number of predictions: "+numPredictions);
                System.out.println("number of mispredictions: "+numGshareMisPredictions);
                System.out.println("misprediction rate: "+fmt.format(missPredictionRate) + "%");
                System.out.println("FINAL GSHARE CONTENTS");
                for (int i=0;i<G_BHT.length;i++)
                {
                    System.out.println(i+"    "+G_BHT[i]);
                }
                break;
            case"hybrid":
                System.out.println("COMMAND");
                System.out.println(argValue);
                System.out.println("OUTPUT");
                System.out.println("number of predictions: "+numPredictions);
                System.out.println("number of mispredictions: "+numHybridMisPredictions);
                System.out.println("misprediction rate: "+fmt.format(missPredictionRate) + "%");
                System.out.println("FINAL CHOOSER CONTENTS");
                for(int i=0; i<CHST.length; i++){
                    System.out.println(i + "   " + CHST[i]);
                }
                System.out.println("FINAL GSHARE CONTENTS");
                for (int i=0;i<G_BHT.length;i++)
                {
                    System.out.println(i+"    "+G_BHT[i]);
                }
                System.out.println("FINAL BIMODAL CONTENTS");
                for(int i=0; i<Bi_BHT.length; i++){
                    System.out.println(i + "   " + Bi_BHT[i]);
                }

                break;
            default: break;
        }



    }

    public static void updGBHR(int h, int act){
        GBHR = GBHR >> 1; // shift out the LSb
        int mask = act << (h-1); // create a bit mask for the actual outcome
        GBHR |= mask;
    }


}


