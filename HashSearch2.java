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
        //��ASCII code ����M mode100
        for(int i=0;i<a.length();i++)
            b += (int)Math.pow(a.charAt(i),2);
        return  b%n;
    }
    //�Nlabel��address�s�JSymbolTable
    public void insert(String a,String b){
        int tmp = hash(a); //��}
        //�P�_�O�_�I��
        if(!sym[tmp].label.equals("")){
            SymbolTable current = sym[tmp];
            do{
                //�P�_�P�@�Ӧ�}��label��address�O�_�@��
                if(current.label.equals(a)){
                    System.out.println("��Label�w�s�b");
                    break;
                }
                if(current.address.equals(b)){
                    System.out.println("��address�w�s�b");
                    break;
                }
                //��̫�@��
                if(current.next!=null){
                    current = current.next;
                    continue;
                }
                //�걵��̫�@�ӫ᭱
                current.next = new SymbolTable();//�걵�s������
                current.next.address = b;//�saddress
                current.next.label = a;//�slabel
                break;
            }while(true);
        }
        else{
            sym[tmp].address = b;//�saddress
            sym[tmp].label = a;//�slabel
        }
    }
    //�j�M�X����address
    public String search(String a){
        int c = hash(a);
        SymbolTable s = sym[c];
        //�����A������ۦPlabel�γ��䤣��
        while(!s.label.equals(a)){
            if(s.next==null)//���᳣̫�S��
                return "�d�߿��~�A�S����Label";
            else//�����
                s = s.next;
        }
        return s.address;//label�@��
    }
    public void symPrint(){
        System.out.println("�}�C��}\tlabel\t    address");
        for(int i=0;i<sym.length;i++){
            if(sym[i].label!="")
                System.out.println(i+"\t\t"+sym[i].label+"\t\t"+sym[i].address);
            if(sym[i].next!=null){
                SymbolTable s = sym[i];
                while(s.next!=null){
                    s = s.next;
                    System.out.println(i+"��C\t\t"+s.label+"\t\t"+s.address);
                }
            }
        }
    }
    public static void main(String[] argv){
        Scanner sc = new Scanner(System.in);
        HashSearch2 h = new HashSearch2(1024);
        System.out.println("Please input Label and Address:");
        String a,b;int i = 0;//a�Olabel�Ab�Oaddress
        System.out.print(++i+"  ");
        while(!(a=sc.next()).equals(".")){
            b=sc.next();
            h.insert(a,b);
            System.out.print(++i+"  ");
        }
        h.symPrint();//�L�X
        System.out.print("�п�JLabel�d��: ");
        while(!(a=sc.next()).equals(".")){
            System.out.println(h.search(a));
            System.out.print("�п�JLabel�d��: ");
        }
    }
}