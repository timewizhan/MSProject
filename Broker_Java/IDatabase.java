import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface IDatabase {

	/* 인터페이스 사용 예 */
	/*
	// 상수 필드
    public final static int MAX_VALUE = 100;
    public final static int MIN_VALUE = 0;

    // 추상 메소드
    public abstract void run();

    // 정적 메소드 : 정적 메소드는 객체 없이도 IDatabase.connectDatabase(); 와 같이 쓸 수 있다.
	public static void change(){
        System.out.println("상태를 변경합니다.");
    }
	*/
	
	public static void connectDatabase(){
		
        try {
    		String driverName = "org.gjt.mm.mysql.Driver"; // 드라이버 이름 지정
            String DBName = "broker_table";
            String dbURL = "jdbc:mysql://localhost:3306/"+DBName; // URL 지정
        	Class.forName(driverName);
			Connection con  = DriverManager.getConnection(dbURL,"root",""); // 연결
	        System.out.println("Mysql DB Connection.");
		
        } catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 드라이버 로드
    }
	
	public static void closeDatabase(){
		
	}
	
}
