import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface IDatabase {

	/* �������̽� ��� �� */
	/*
	// ��� �ʵ�
    public final static int MAX_VALUE = 100;
    public final static int MIN_VALUE = 0;

    // �߻� �޼ҵ�
    public abstract void run();

    // ���� �޼ҵ� : ���� �޼ҵ�� ��ü ���̵� IDatabase.connectDatabase(); �� ���� �� �� �ִ�.
	public static void change(){
        System.out.println("���¸� �����մϴ�.");
    }
	*/
	
	public static void connectDatabase(){
		
        try {
    		String driverName = "org.gjt.mm.mysql.Driver"; // ����̹� �̸� ����
            String DBName = "broker_table";
            String dbURL = "jdbc:mysql://localhost:3306/"+DBName; // URL ����
        	Class.forName(driverName);
			Connection con  = DriverManager.getConnection(dbURL,"root",""); // ����
	        System.out.println("Mysql DB Connection.");
		
        } catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // ����̹� �ε�
    }
	
	public static void closeDatabase(){
		
	}
	
}
