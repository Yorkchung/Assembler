import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
public class HashTable{
    private Table[] t;
    private int n;
    private String mnOrLabel;
    private String opOrAddress;
    private int ptr;//作為查詢位定義location的指標
    public class Table{//內部類別，存Table
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
    public class NoLabel{//內部類別，存需重新定址的Address
        public String address;//要修改的位置
        public int length;//長度
        public NoLabel next;
        public NoLabel(){
            address = "";
            length = 0;
            next = null;
        }
    }
    public HashTable(int l,String a,String b){//長度、字串AB
        this.n = l;
        this.mnOrLabel=a;this.opOrAddress=b;
        t = new Table[n];
        for(int i=0;i<n;i++)
            t[i] = new Table();
    }
    public HashTable(String txt,int l,String a,String b)throws IOException{//檔案、長度、字串AB
        this(l,a,b);
        FileReader fr = new FileReader(txt);
        BufferedReader br = new BufferedReader(fr);
        String line;
        //讀入txt，分割並丟入insert
        while((line=br.readLine())!=null){
            String[] arr = line.split(" ");
            insert(arr[0],arr[1]);
        }
        fr.close();
    }
    //Hash function
    public int hash(String a){
        int b =0;
        //取ASCII code 平方和 mode陣列長度
        for(int i=0;i<a.length();i++)
            b += (int)Math.pow(a.charAt(i),2);
        return  b%n;
    }
    //將input及output存入Table
    public boolean insert(String a,String b){//回傳是否儲存成功
        b = b.toUpperCase();
        int tmp = hash(a); //位址
        //判斷是否碰撞
        if(!t[tmp].input.equals("")){
            Table current = t[tmp];
            do{
                //判斷同一個位址的input及output是否一樣
                if(current.input.equals(a)){
                    System.out.print(a+": 此"+mnOrLabel+"已存在");
                    return false;
                }
                if(current.output.equals(b)){
                    System.out.print(b+": 此"+opOrAddress+"已存在");
                    return false;
                }
                //找最後一個
                if(current.next!=null){
                    current = current.next;
                    continue;
                }
                //串接到最後一個後面
                current.next = new Table();//串接新的物件
                current.next.output = b;//存output
                current.next.input = a;//存input
                break;
            }while(true);
        }
        else{
            t[tmp].output = b;//存output
            t[tmp].input = a;//存input
        }
        return true;
    }
    //搜尋出對應output
    public String search(String a){
        int c = hash(a);
        Table s = t[c];
        //往後找，直到找到相同input或都找不到
        while(!s.input.equals(a)){
            if(s.next==null)//找到最後都沒有
                return "false";
            else//往後找
                s = s.next;
        }
        return s.output;//input一樣
    }
    public void tPrint(){
        System.out.println("陣列位址\t"+mnOrLabel+"\t  "+opOrAddress);
        for(int i=0;i<t.length;i++){
            if(t[i].input!="")
                System.out.println(i+"\t\t"+t[i].input+"\t\t"+t[i].output);
            if(t[i].next!=null){
                Table s = t[i];
                while(s.next!=null){
                    s = s.next;
                    System.out.println(i+"串列\t\t"+s.input+"\t\t"+s.output);
                }
            }
        }
    }
    //如果沒有Label，存要修改的位置
    public void storeLabel(String a,String address){//Label、address
        int c = hash(a);
        Table s = t[c];
        while(!s.input.equals(a))//往後找到對應的Label
            s = s.next;
        int i=0;
        while(true){
            NoLabel no = s.noLabel;
            if(s.noLabel==null){//第一個
                s.noLabel = new NoLabel();
                no = s.noLabel;
            }else if(!no.address.equals(address)){//找到最後一個位置，存要修改的address
                while(no.next!=null){
                    no = no.next;
                }
                no.next = new NoLabel();
                no = no.next;
            }else{
                System.out.println("此行已讀過");
                break;
            }   
            no.address = address;//要修改的位置
            no.length = address.length();//要修改的長度
            break;
        }
    }
    //搜尋noLabel
    public String searchNoLabel(String a,String address){//Label、現在位址
        int c = hash(a);
        Table s = t[c];
        while(!s.input.equals(a))//往後找到對應的Label
            s = s.next;
        s.output = address;
        NoLabel no = s.noLabel;
        if(ptr==0){//第一個
            ptr++;
            return s.noLabel.address;
        }
        for(int i=0;i<ptr;i++){//指標往下找
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
        String a,b;int i = 0;//a是label，b是address
        System.out.print(++i+"  ");
        while(!(a=sc.next()).equals(".")){
            b=sc.next();
            h.insert(a,b);
            System.out.print(++i+"  ");
        }
        h.tPrint();//印出
        System.out.print("請輸入查詢: ");
        while(!(a=sc.next()).equals(".")){
            System.out.println(h.search(a));
            System.out.print("請輸入查詢: ");
        }
    }
}