package dao;

import common.DbHelper;

public class BankDAO {
	DbHelper db=new DbHelper();
	/**
	 * 存款
	 * @param cardno
	 * @param money
	 */
	public void update(String cardno,float money){
		String sql="update account set balance=balance + ? where accountid=?";
		db.update(sql, money,cardno);
	}
	/**
	 * 取款
	 * @param cardno
	 * @param money
	 */
	public void update1(String cardno,float money){
		String sql="update account set balance=balance - ? where accountid=?";
		db.update(sql, money,cardno);
	}
	
	public void remove(String cardno,String cardno2,float money){
		String sql="";
	}
}
