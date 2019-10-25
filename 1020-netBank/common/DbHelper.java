package common;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class DbHelper {
		private Connection conn;
		private PreparedStatement pstmt;
		private ResultSet rs;
		static{//执行静态类
			//加载驱动
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		}
		//获取数据库的连接
		public Connection getConn() throws SQLException{
			conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/bank","root","lbj");
			return conn;
		}
		//关闭资源
		public void closeAll(Connection conn,PreparedStatement pstmt,ResultSet rs){
			//关闭资源
			if(conn!=null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(pstmt!=null){
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
		/**
		 * 返回多条记录查询操作 select* from guanli
		 * sql	
		 * params
		 * @throws IOException 
		 * @throws SQLException 
		 */
		public List<Map<String,Object>> selectMutil(String sql,List<Object>params) throws IOException, SQLException{
			List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
			Map<String,Object> map=null;
			try {
				conn=getConn();
				pstmt=conn.prepareStatement(sql);
				//设置参数
				setParamsList(pstmt,params);
				//获取结果集
				rs=pstmt.executeQuery();
				//根据结果集对象获得所有结果集中所有列名
				List<String> columnNames=getAllColumnNames(rs);
				while(rs.next()){
					map=new HashMap<String,Object>();
					//循环所有的列名
					for(String name:columnNames){
						String typeName=null; //值的类型
						Object obj=null;//获取的值
						obj=rs.getObject(name);
						if(null!=obj){
							typeName=obj.getClass().getName();
						}
						if("oracle.sql.BLOB".equals(typeName)){
//							//对图片进行处理
//							BLOB blob=(BLOB)obj;
//							InputStream in=blob.getBinaryStream();
//							byte bt[]=new byte[(int) blob.length()];
//							in.read(bt);
//							map.put(name, bt);//将blob类型值以字节数组的形式存储
//						}else{
							map.put(name, obj);
						}
					}
					list.add(map);
				}
			} finally{
				closeAll(conn,pstmt,rs);
			}
		return list;
		}
		/**
		 * 单记录的查询select *from table_name where id=?
		 * @param sql 查询的sql语句
		 * @param params 传入的参数 集合中 集合的参数顺序必须和？顺序一致
		 * @return
		 * @throws SQLException 
		 * @throws IOException 
		 */
		public Map<String,Object> selectSingle(String sql,List<Object> params) throws SQLException, IOException{
			Map<String,Object> map=null;
			try {
				conn=getConn();
				pstmt=conn.prepareStatement(sql);
				//设置参数
				setParamsList(pstmt,params);
				//获取结果集
				rs=pstmt.executeQuery();
				//根据结果集对象获得所有结果集中所有列名
				List<String> columnNames=getAllColumnNames(rs);
				if(rs.next()){
					map=new HashMap<String,Object>();
					String typeName=null; //值的类型
					Object obj=null;//获取的值
					//循环所有的列名
					for(String name:columnNames){
						obj=rs.getObject(name);
						if(null!=obj){
							typeName=obj.getClass().getName();//返回的类型
						}
						else{
							continue;
						}
						if("oracle.sql.BLOB".equals(typeName)){//判断的是是否是U-image 是会进行赋值
//							//对图片进行处理
//							BLOB blob=(BLOB)obj;
//							InputStream in=blob.getBinaryStream();
//							byte bt[]=new byte[(int) blob.length()];
//							in.read(bt);
//							map.put(name, bt);//将blob类型值以字节数组的形式存储
//						}else{
							map.put(name, obj);
						}
					}
				}
			} finally{
				closeAll(conn,pstmt,rs);
			}
			return map;
		}
		/**
		 * 
		 * 获取查询后的字段名
		 * @param result
		 * @return
		 * @throws SQLException 
		 */
		public List<String> getAllColumnNames( ResultSet rs) throws SQLException {
			List<String> list=new ArrayList<String>();
			ResultSetMetaData data =rs.getMetaData();
			int count=data.getColumnCount();//返回列数目
			for(int i=1;i<=count;i++){
				String str=data.getColumnName(i);//获取指定列的名称
				//添加列名到List集合中
				list.add(str);
			}
			return list;
		}
		//将集合设置到预编译对象中
		public void setParamsList(PreparedStatement pstmt, List<Object> params) throws SQLException {
			if(null==params||params.size()<=0){
				return;
			}
			for(int i=0;i<params.size();i++){
				pstmt.setObject(i+1, params.get(i));
			}
			
		}
		/**
		 * 批处理操作 多个insert update dalete 同一个事务
		 * sqls 多条sql语句
		 * params 多条sql语句的参数 每条sql语句参数小List 集合中多个再封装到大的list集合一一对应
		 * 
		 */
		public int update(List<String>sqls,List<List<Object>> params)throws Exception{
			int result=0;
			try{
				conn=getConn();
				//设置事务手动提交
				conn.setAutoCommit(false);
				//循环sql语句
				if(null==sqls||sqls.size()<=0){
					return result;
				}
				for(int i=0;i<sqls.size();i++){
					//获取sql语句并创建预编译对象
					pstmt=conn.prepareStatement(sqls.get(i));
					//获取对应的sql语句参数集合
					List<Object> param=params.get(i);
					//设置参数
					setParamsList(pstmt,param);
					//执行更新
					result=pstmt.executeUpdate();
					if(result<=0){
						return result;
					}
				}
				//手动提交
				conn.commit();
			}catch(Exception e){
				//设置回滚
				conn.rollback();
				result=0;
			}finally{
				//还原事务的状态
				conn.setAutoCommit(true);
				closeAll(conn,pstmt,rs);
			}
			
			
			return result;
		}
		
		
		/**
		 * 更新操作  增删改
		 * sql 更新语句
		 * params 传入的参数  不定长的对象数组  传入的参数的顺序与？顺序一致
		 */
		public int update(String sql,Object...params){
			int result=0;
			try {
				conn=getConn();//获取连接对象
				pstmt=conn.prepareStatement(sql);
				//设置参数
				setParamsObject(pstmt,params);
				//执行
				result=pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				closeAll(conn,pstmt,null);
			}
			
			return result;
		}
		private void setParamsObject(PreparedStatement pstmt, Object... params) throws SQLException {
			if(null==params||params.length<0){
				return;
			}
			for(int i=0;i<params.length;i++){
				pstmt.setObject(i+1, params[i]);//将数组中第i个元素设置为第i+1个问号   从1开始赋值才对的
			}	
		}
		/**
		 * 聚合函数操作 select count(*) from guanli
		 * @param sql
		 * @param params
		 * @return
		 */
		public double getPolymer(String sql, List<Object> params) {
			double result=0;
			try {
				conn=getConn();
				pstmt=conn.prepareStatement(sql);
				setParamsList(pstmt,params);
				rs=pstmt.executeQuery();
				if(rs.next()){
					result=rs.getDouble(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}finally{
				closeAll(conn,pstmt,rs);
			}
			return result;
		}
		/**
		 * 返回一行数据
		 * 例如Map
		 * @param sql
		 * @param params
		 * @param cls
		 * @return
		 * @throws Exception
		 */
		public <T> T findSingle(String sql,List<Object> params,Class<T> cls)throws Exception{
			T t=null;
			try{
			conn=getConn();
			pstmt=conn.prepareStatement(sql);
			//设置参数
			setParamsList(pstmt,params);
			rs=pstmt.executeQuery();
			//通过反射获取实体类中给所有的方法
			Method methods[]=cls.getDeclaredMethods();
			//通过反射获取实体类中所有的属性
			List<String> columnNames=getAllColumnNames(rs);
			Object obj=null;
			if(rs.next()){
				//创建对象  通过反射
				t=cls.newInstance();//默认调用对象的无参数的构造函数
				//循环列
				for(String name:columnNames){
					obj=rs.getObject(name);//获取值
					//System.out.println(name);
					
					//循环方法 set+name setUname
					for(Method m:methods){
						//System.out.println(m.getName());
						if(("set"+name).equalsIgnoreCase(m.getName())){
							//set方法的形参类型进行判断
							String typeName=m.getParameterTypes()[0].getName();
							//System.out.println(typeName);
							if("java.lang.Integer".equals(typeName)){
								m.invoke(t,rs.getInt(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
							}else if("java.lang.Double".equals(typeName)){
								m.invoke(t,rs.getDouble(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
							}else if("java.lang.Float".equals(typeName)){
								m.invoke(t,rs.getFloat(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
							}else if("java.lang.Long".equals(typeName)){
								m.invoke(t,rs.getLong(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
							}else{
								m.invoke(t,rs.getString(name));
								}
							}
						}
					}
				}
			}finally{
					closeAll(conn,pstmt,rs);
				}
			return t;
		}
		
		/**查询语句  返回多条记录
		 * List<T> 返回
		 * @param sql
		 * @param params
		 * @param cls
		 * @return
		 * @throws Exception
		 */
		public <T>  List<T> findMutil(String sql,List<Object> params, Class<T> cls)throws Exception{
			List<T> list=new ArrayList<T>();
			T t=null;
			try{
				conn=getConn();
				pstmt=conn.prepareStatement(sql);
				//设置参数
				setParamsList(pstmt,params);
				rs=pstmt.executeQuery();
				//通过反射获取实体类中给所有的方法
				Method methods[]=cls.getDeclaredMethods();
				//通过反射获取实体类中所有的属性
				List<String> columnNames=getAllColumnNames(rs);
				Object obj=null;
				while(rs.next()){
					//创建对象  通过反射
					t=cls.newInstance();//默认调用对象的无参数的构造函数
					//循环列
					for(String name:columnNames){
						obj=rs.getObject(name);//获取值
						//System.out.println(name);
						
						//循环方法 set+name setUname
						for(Method m:methods){
							//System.out.println(m.getName());
							if(("set"+name).equalsIgnoreCase(m.getName())){
								//set方法的形参类型进行判断
								String typeName=m.getParameterTypes()[0].getName();
								//System.out.println(typeName);
								if("java.lang.Integer".equals(typeName)){
									m.invoke(t,rs.getInt(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
								}else if("java.lang.Double".equals(typeName)){
									m.invoke(t,rs.getDouble(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
								}else if("java.lang.Float".equals(typeName)){
									m.invoke(t,rs.getFloat(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
								}else if("java.lang.Long".equals(typeName)){
									m.invoke(t,rs.getLong(name));//激活此方法，传入的参数必须和底层方法的数据类型一致
								}else{
									m.invoke(t,rs.getString(name));
									}
								}
							}
						}
					//将对象添加到List集合中
					list.add(t);
					}
				}finally{
						closeAll(conn,pstmt,rs);
					}
				return list;
			}
}
