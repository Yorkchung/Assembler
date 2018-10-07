import java.util.Scanner;
class SymbolTable{
    public String label;
    public String address;
    public SymbolTable next;
    public SymbolTable(){
        label = "";
        address = "";
        next = null;
    }
}
public class HashSearch2{
    static SymbolTable[] sym;
    private int n;
    public HashSearch2(int l){
        n = l;
        sym = new SymbolTable[n];
        for(int i=0;i<n;i++)
            sym[i] = new SymbolTable();
    }
    //Hash function
    public int hash(String a){
        int b =0;
        //取ASCII code 平方和 mode100
        for(int i=0;i<a.length();i++)
            b += (int)Math.pow(a.charAt(i),2);
        return  b%n;
    }
    //將label及address存入SymbolTable
    public void insert(String a,String b){
        int tmp = hash(a); //位址
        //判斷是否碰撞
        if(!sym[tmp].label.equals("")){
            SymbolTable current = sym[tmp];
            do{
                //判斷同一個位址的label及address是否一樣
                if(current.label.equals(a)){
                    System.out.println("此Label已存在");
                    break;
                }
                if(current.address.equals(b)){
                    System.out.println("此address已存在");
                    break;
                }
                //找最後一個
                if(current.next!=null){
                    current = current.next;
                    continue;
                }
                //串接到最後一個後面
                current.next = new SymbolTable();//串接新的物件
                current.next.address = b;//存address
                current.next.label = a;//存label
                break;
            }while(true);
        }
        else{
            sym[tmp].address = b;//存address
            sym[tmp].label = a;//存label
        }
    }
    //搜尋出對應address
    public String search(String a){
        int c = hash(a);
        SymbolTable s = sym[c];
        //往後找，直到找到相同label或都找不到
        while(!s.label.equals(a)){
            if(s.next==null)//找到最後都沒有
                return "查詢錯誤，沒有此Label";
            else//往後找
                s = s.next;
        }
        return s.address;//label一樣
    }
    public void symPrint(){
        System.out.println("陣列位址\tlabel\t    address");
        for(int i=0;i<sym.length;i++){
            if(sym[i].label!="")
                System.out.println(i+"\t\t"+sym[i].label+"\t\t"+sym[i].address);
            if(sym[i].next!=null){
                SymbolTable s = sym[i];
                while(s.next!=null){
                    s = s.next;
                    System.out.println(i+"串列\t\t"+s.label+"\t\t"+s.address);
                }
            }
        }
    }
    public static void main(String[] argv){
        Scanner sc = new Scanner(System.in);
        HashSearch2 h = new HashSearch2(1024);
        System.out.println("Please input Label and Address:");
        String a,b;int i = 0;//a是label，b是address
        System.out.print(++i+"  ");
        while(!(a=sc.next()).equals(".")){
            b=sc.next();
            h.insert(a,b);
            System.out.print(++i+"  ");
        }
        h.symPrint();//印出
        System.out.print("請輸入Label查詢: ");
        while(!(a=sc.next()).equals(".")){
            System.out.println(h.search(a));
            System.out.print("請輸入Label查詢: ");
        }
    }
}