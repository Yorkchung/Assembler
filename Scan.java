import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
public class Scan{
    HashSearch op;//Ū�JopCode
    HashSearch2 sym = new HashSearch2(1024);
    private int address;
    public Scan()throws IOException{
        op = new HashSearch("opCode.txt");/*Ū�JopCode****/
    }
    //Ū�J�զX�y��
    public void read(String data)throws IOException{
        FileReader fr = new FileReader(data);
        BufferedReader br = new BufferedReader(fr);
        String line;
        for(int j=1;(line=br.readLine())!=null;j++){//�@��Ū�A����Ū���ɮ׳̫�
            System.out.printf("%d:%s\n",j,line);
            line = line.replaceAll("[\\.].*$","");//�R������
            line = line.trim();//�R���e��ť�
            if(line.trim().length()<=0)//�����ťզ�
                continue;
            String[] arr = line.split("[,\\s]+");//�Ϊťդ�,����
            this.putSym(arr);
            System.out.println();
        }
    }
    //�s�iSymbolTable�A���K�L�XLabel�Bmnemonic...
    public void putSym(String[] arr){
        if(op.search(arr[0])=="false"){//�Ĥ@�Ӥ��Omnemonic
            if(arr.length==1&&!arr[0].equals("RSUB")){
                System.out.printf("You have wrong code!\n");
                return;
            }
            if(arr[1].equals("START")){
                address = Integer.parseInt(arr[2],16);//16�i����10�i��
                System.out.printf("Program name is %s\nstart from this line\n",arr[0]);
            }else if(arr[0].equals("END"))
                System.out.println("end of the program");
            else{
                if(arr[1].equals("RESB")){
                    sym.insert(arr[0],Integer.toHexString(address));//10�i����16�i��
                    address += Integer.parseInt(arr[2]);
                    System.out.println(arr[1]+" is pesudo instruction code");
                }else if(arr[1].equals("RESW")){
                    sym.insert(arr[0],Integer.toHexString(address));//10�i����16�i��
                    address += 3*Integer.parseInt(arr[2],16);
                    System.out.println(arr[1]+" is pesudo instruction code");
                }else if(arr[1].equals("WORD")){
                    sym.insert(arr[0],Integer.toHexString(address));//10�i����16�i��
                    address += 3;
                }else if(arr[1].equals("BYTE")){
                    sym.insert(arr[0],Integer.toHexString(address));//10�i����16�i��
                    address +=(arr[2].charAt(0)=='X')?(arr[2].length()-3)/2:(arr[2].length()-3);//�P�_�OX'..'�٬OC'..'
                }else if(op.search(arr[1])!="false"&&!arr[1].equals("RSUB")){//�Omnemonic
                    sym.insert(arr[0],Integer.toHexString(address));//10�i����16�i��
                    //�P�_�����ζ����w�}
                    System.out.println(arr[arr.length-1].equals("X")?"It's indexed addressing!":"It's direct addressing!");
                    System.out.printf("Label: %s\t Mnemonic: %s\t Operand: %s\n",arr[0],arr[1],arr[2]);
                    address += 3;
                }else//�����ŦX�A�{�����~
                    System.out.printf("You have wrong code!\n");
            }
        }else{
            address += 3;
            if(!arr[0].equals("RSUB")){//�P�_�DRSUB��Mnemonic
                //�P�_�����ζ����w�}
                System.out.println(arr[arr.length-1].equals("X")?"It's indexed addressing!":"It's direct addressing!");
                System.out.printf("Label: \t Mnemonic: %s\t Operand: %s\n",arr[0],arr[1]);
            }else if(arr.length==1)
                System.out.printf("Label: \t Mnemonic: %s\t Operand: \n",arr[0]);
            else
                System.out.printf("You have wrong code!\n");
        }
    }
    public void printSym(){
        sym.symPrint();//�L�XSymbolTable
    }
    public static void main(String[] argv)throws IOException{
        //Ū�J�զX�y��
        Scan s = new Scan();
        //s.read("testprog.S");
        s.read("try.txt");
        s.printSym();
    }
}