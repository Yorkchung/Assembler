import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
//node結構
class SICTable{
    public String mnemonic;
    public String opCode;
    public SICTable next;
    public SICTable(){
        mnemonic = "";
        opCode = "";
        next = null;
    }
}
//Hash搜尋
public class HashSearch{
    SICTable[] sic = new SICTable[100];
    //建立SICTable
    public HashSearch(){
        for(int i=0;i<100;i++)
            sic[i] = new SICTable();
    }
    public HashSearch(String txt)throws IOException{
        this();
        FileReader fr = new FileReader(txt);
        BufferedReader br = new BufferedReader(fr);
        String line;
        //讀入txt，分割並丟入insert
        while((line=br.readLine())!=null){
            String[] arr = line.split(" ");
            insert(arr[0],arr[1]);
        }
    }
    //Hash function
    public int hash(String a){
        int b =0;
        //取ASCII code 相加 mode100
        for(int i=0;i<a.length();i++)
            b += (int)a.charAt(i);
        return  b%100;
    }
    //將mnemonic及opCode存入SICTable
    public void insert(String a,String b){
        int tmp = hash(a); //位址
        while(true){
            //判斷是否碰撞
            if(!sic[tmp].mnemonic.equals("")){
                SICTable current = sic[tmp];
                //接到最後
                while(current.next!=null){
                    current = current.next;
                }
                current.next = new SICTable();//串接新的物件
                current.next.opCode = b;//存opCode
                current.next.mnemonic = a;//存mnemonic
                break;
            }else{
                sic[tmp].opCode = b;//存opCode
                sic[tmp].mnemonic = a;//存mnemonic
                break;
            }
        }
    }
    //搜尋出對應opCode
    public String search(String a){
        int c = hash(a);
        SICTable s = sic[c];
        //往後找，直到找到相同mnemonic或都找不到
        while(!s.mnemonic.equals(a)){
            if(s.next==null)//找到最後都沒有
                return "false";
            else//往後找
                s = s.next;
        }
        return s.opCode;//label一樣
    }
    //印出SICTable
    public void SICprint(){
        System.out.println("位址\t     mnemonic\t     opCode");
        for(int i=0;i<sic.length;i++){
            if(sic[i].mnemonic!="")
                System.out.println(i+"\t\t"+sic[i].mnemonic+"\t\t"+sic[i].opCode);
            if(sic[i].next!=null){
                SICTable s = sic[i];
                while(s.next!=null){
                    s = s.next;
                    System.out.println(i+"串列\t\t"+s.mnemonic+"\t\t"+s.opCode);
                }
            }
        }
    }
    public static void main(String[] argv)throws IOException{
        HashSearch h = new HashSearch("opCode.txt");
        h.SICprint();
        System.out.print("請輸入查詢的mnemonic:");
        System.out.println("opCode: "+h.search(new Scanner(System.in).next().toUpperCase()));
    }
}