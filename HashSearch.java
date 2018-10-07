import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
//node���c
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
//Hash�j�M
public class HashSearch{
    SICTable[] sic = new SICTable[100];
    //�إ�SICTable
    public HashSearch(){
        for(int i=0;i<100;i++)
            sic[i] = new SICTable();
    }
    public HashSearch(String txt)throws IOException{
        this();
        FileReader fr = new FileReader(txt);
        BufferedReader br = new BufferedReader(fr);
        String line;
        //Ū�Jtxt�A���Ψå�Jinsert
        while((line=br.readLine())!=null){
            String[] arr = line.split(" ");
            insert(arr[0],arr[1]);
        }
    }
    //Hash function
    public int hash(String a){
        int b =0;
        //��ASCII code �ۥ[ mode100
        for(int i=0;i<a.length();i++)
            b += (int)a.charAt(i);
        return  b%100;
    }
    //�Nmnemonic��opCode�s�JSICTable
    public void insert(String a,String b){
        int tmp = hash(a); //��}
        while(true){
            //�P�_�O�_�I��
            if(!sic[tmp].mnemonic.equals("")){
                SICTable current = sic[tmp];
                //����̫�
                while(current.next!=null){
                    current = current.next;
                }
                current.next = new SICTable();//�걵�s������
                current.next.opCode = b;//�sopCode
                current.next.mnemonic = a;//�smnemonic
                break;
            }else{
                sic[tmp].opCode = b;//�sopCode
                sic[tmp].mnemonic = a;//�smnemonic
                break;
            }
        }
    }
    //�j�M�X����opCode
    public String search(String a){
        int c = hash(a);
        SICTable s = sic[c];
        //�����A������ۦPmnemonic�γ��䤣��
        while(!s.mnemonic.equals(a)){
            if(s.next==null)//���᳣̫�S��
                return "false";
            else//�����
                s = s.next;
        }
        return s.opCode;//label�@��
    }
    //�L�XSICTable
    public void SICprint(){
        System.out.println("��}\t     mnemonic\t     opCode");
        for(int i=0;i<sic.length;i++){
            if(sic[i].mnemonic!="")
                System.out.println(i+"\t\t"+sic[i].mnemonic+"\t\t"+sic[i].opCode);
            if(sic[i].next!=null){
                SICTable s = sic[i];
                while(s.next!=null){
                    s = s.next;
                    System.out.println(i+"��C\t\t"+s.mnemonic+"\t\t"+s.opCode);
                }
            }
        }
    }
    public static void main(String[] argv)throws IOException{
        HashSearch h = new HashSearch("opCode.txt");
        h.SICprint();
        System.out.print("�п�J�d�ߪ�mnemonic:");
        System.out.println("opCode: "+h.search(new Scanner(System.in).next().toUpperCase()));
    }
}