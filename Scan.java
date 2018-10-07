import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
public class Scan{
    HashSearch op;//讀入opCode
    HashSearch2 sym = new HashSearch2(1024);
    private int address;
    public Scan()throws IOException{
        op = new HashSearch("opCode.txt");/*讀入opCode****/
    }
    //讀入組合語言
    public void read(String data)throws IOException{
        FileReader fr = new FileReader(data);
        BufferedReader br = new BufferedReader(fr);
        String line;
        for(int j=1;(line=br.readLine())!=null;j++){//一直讀，直到讀到檔案最後
            System.out.printf("%d:%s\n",j,line);
            line = line.replaceAll("[\\.].*$","");//刪除註解
            line = line.trim();//刪除前後空白
            if(line.trim().length()<=0)//忽略空白行
                continue;
            String[] arr = line.split("[,\\s]+");//用空白及,分割
            this.putSym(arr);
            System.out.println();
        }
    }
    //存進SymbolTable，順便印出Label、mnemonic...
    public void putSym(String[] arr){
        if(op.search(arr[0])=="false"){//第一個不是mnemonic
            if(arr.length==1&&!arr[0].equals("RSUB")){
                System.out.printf("You have wrong code!\n");
                return;
            }
            if(arr[1].equals("START")){
                address = Integer.parseInt(arr[2],16);//16進位轉10進位
                System.out.printf("Program name is %s\nstart from this line\n",arr[0]);
            }else if(arr[0].equals("END"))
                System.out.println("end of the program");
            else{
                if(arr[1].equals("RESB")){
                    sym.insert(arr[0],Integer.toHexString(address));//10進位轉16進位
                    address += Integer.parseInt(arr[2]);
                    System.out.println(arr[1]+" is pesudo instruction code");
                }else if(arr[1].equals("RESW")){
                    sym.insert(arr[0],Integer.toHexString(address));//10進位轉16進位
                    address += 3*Integer.parseInt(arr[2],16);
                    System.out.println(arr[1]+" is pesudo instruction code");
                }else if(arr[1].equals("WORD")){
                    sym.insert(arr[0],Integer.toHexString(address));//10進位轉16進位
                    address += 3;
                }else if(arr[1].equals("BYTE")){
                    sym.insert(arr[0],Integer.toHexString(address));//10進位轉16進位
                    address +=(arr[2].charAt(0)=='X')?(arr[2].length()-3)/2:(arr[2].length()-3);//判斷是X'..'還是C'..'
                }else if(op.search(arr[1])!="false"&&!arr[1].equals("RSUB")){//是mnemonic
                    sym.insert(arr[0],Integer.toHexString(address));//10進位轉16進位
                    //判斷直接或間接定址
                    System.out.println(arr[arr.length-1].equals("X")?"It's indexed addressing!":"It's direct addressing!");
                    System.out.printf("Label: %s\t Mnemonic: %s\t Operand: %s\n",arr[0],arr[1],arr[2]);
                    address += 3;
                }else//都不符合，程式錯誤
                    System.out.printf("You have wrong code!\n");
            }
        }else{
            address += 3;
            if(!arr[0].equals("RSUB")){//判斷非RSUB的Mnemonic
                //判斷直接或間接定址
                System.out.println(arr[arr.length-1].equals("X")?"It's indexed addressing!":"It's direct addressing!");
                System.out.printf("Label: \t Mnemonic: %s\t Operand: %s\n",arr[0],arr[1]);
            }else if(arr.length==1)
                System.out.printf("Label: \t Mnemonic: %s\t Operand: \n",arr[0]);
            else
                System.out.printf("You have wrong code!\n");
        }
    }
    public void printSym(){
        sym.symPrint();//印出SymbolTable
    }
    public static void main(String[] argv)throws IOException{
        //讀入組合語言
        Scan s = new Scan();
        //s.read("testprog.S");
        s.read("try.txt");
        s.printSym();
    }
}