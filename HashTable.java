import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
public class HashTable{
    private Table[] t;
    private int n;
    private String mnOrLabel;
    private String opOrAddress;
    private int ptr;//�@���d�ߦ�w�qlocation������
    public class Table{//�������O�A�sTable
        public String input;//mnemonic or label
        public String output;//opCode or address
        public Table next;
        public NoLabel noLabel;
        public Table(){
            input = "";
            output = "";
            next = null;
            noLabel = null;
        }
    }
    public class NoLabel{//�������O�A�s�ݭ��s�w�}��Address
        public String address;//�n�ק諸��m
        public int length;//����
        public NoLabel next;
        public NoLabel(){
            address = "";
            length = 0;
            next = null;
        }
    }
    public HashTable(int l,String a,String b){//���סB�r��AB
        this.n = l;
        this.mnOrLabel=a;this.opOrAddress=b;
        t = new Table[n];
        for(int i=0;i<n;i++)
            t[i] = new Table();
    }
    public HashTable(String txt,int l,String a,String b)throws IOException{//�ɮסB���סB�r��AB
        this(l,a,b);
        FileReader fr = new FileReader(txt);
        BufferedReader br = new BufferedReader(fr);
        String line;
        //Ū�Jtxt�A���Ψå�Jinsert
        while((line=br.readLine())!=null){
            String[] arr = line.split(" ");
            insert(arr[0],arr[1]);
        }
        fr.close();
    }
    //Hash function
    public int hash(String a){
        int b =0;
        //��ASCII code ����M mode�}�C����
        for(int i=0;i<a.length();i++)
            b += (int)Math.pow(a.charAt(i),2);
        return  b%n;
    }
    //�Ninput��output�s�JTable
    public boolean insert(String a,String b){//�^�ǬO�_�x�s���\
        b = b.toUpperCase();
        int tmp = hash(a); //��}
        //�P�_�O�_�I��
        if(!t[tmp].input.equals("")){
            Table current = t[tmp];
            do{
                //�P�_�P�@�Ӧ�}��input��output�O�_�@��
                if(current.input.equals(a)){
                    System.out.print(a+": ��"+mnOrLabel+"�w�s�b");
                    return false;
                }
                if(current.output.equals(b)){
                    System.out.print(b+": ��"+opOrAddress+"�w�s�b");
                    return false;
                }
                //��̫�@��
                if(current.next!=null){
                    current = current.next;
                    continue;
                }
                //�걵��̫�@�ӫ᭱
                current.next = new Table();//�걵�s������
                current.next.output = b;//�soutput
                current.next.input = a;//�sinput
                break;
            }while(true);
        }
        else{
            t[tmp].output = b;//�soutput
            t[tmp].input = a;//�sinput
        }
        return true;
    }
    //�j�M�X����output
    public String search(String a){
        int c = hash(a);
        Table s = t[c];
        //�����A������ۦPinput�γ��䤣��
        while(!s.input.equals(a)){
            if(s.next==null)//���᳣̫�S��
                return "false";
            else//�����
                s = s.next;
        }
        return s.output;//input�@��
    }
    public void tPrint(){
        System.out.println("�}�C��}\t"+mnOrLabel+"\t  "+opOrAddress);
        for(int i=0;i<t.length;i++){
            if(t[i].input!="")
                System.out.println(i+"\t\t"+t[i].input+"\t\t"+t[i].output);
            if(t[i].next!=null){
                Table s = t[i];
                while(s.next!=null){
                    s = s.next;
                    System.out.println(i+"��C\t\t"+s.input+"\t\t"+s.output);
                }
            }
        }
    }
    //�p�G�S��Label�A�s�n�ק諸��m
    public void storeLabel(String a,String address){//Label�Baddress
        int c = hash(a);
        Table s = t[c];
        while(!s.input.equals(a))//�����������Label
            s = s.next;
        int i=0;
        while(true){
            NoLabel no = s.noLabel;
            if(s.noLabel==null){//�Ĥ@��
                s.noLabel = new NoLabel();
                no = s.noLabel;
            }else if(!no.address.equals(address)){//���̫�@�Ӧ�m�A�s�n�ק諸address
                while(no.next!=null){
                    no = no.next;
                }
                no.next = new NoLabel();
                no = no.next;
            }else{
                System.out.println("����wŪ�L");
                break;
            }   
            no.address = address;//�n�ק諸��m
            no.length = address.length();//�n�ק諸����
            break;
        }
    }
    //�j�MnoLabel
    public String searchNoLabel(String a,String address){//Label�B�{�b��}
        int c = hash(a);
        Table s = t[c];
        while(!s.input.equals(a))//�����������Label
            s = s.next;
        s.output = address;
        NoLabel no = s.noLabel;
        if(ptr==0){//�Ĥ@��
            ptr++;
            return s.noLabel.address;
        }
        for(int i=0;i<ptr;i++){//���Щ��U��
            if(no.next==null){
                ptr = 0;
                return null;
            }
            no = no.next;
        }
        ptr++;
        return no.address;
    }
    public static void main(String[] argv){
        Scanner sc = new Scanner(System.in);
        HashTable h = new HashTable(1024,"label","address");
        String a,b;int i = 0;//a�Olabel�Ab�Oaddress
        System.out.print(++i+"  ");
        while(!(a=sc.next()).equals(".")){
            b=sc.next();
            h.insert(a,b);
            System.out.print(++i+"  ");
        }
        h.tPrint();//�L�X
        System.out.print("�п�J�d��: ");
        while(!(a=sc.next()).equals(".")){
            System.out.println(h.search(a));
            System.out.print("�п�J�d��: ");
        }
    }
}