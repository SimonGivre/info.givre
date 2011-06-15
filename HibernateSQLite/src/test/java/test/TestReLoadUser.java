package test;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import model.User;

import org.hibernate.Session;

import util.HibernateUtil;

public class TestReLoadUser extends TestCase {

	public void testReloadUser(){
		//UserDao userDao = new UserDao();
		
		//User user = userDao.find(4);
		Session session = HibernateUtil.getSessionFactory().openSession();
        //Transaction tx = session.beginTransaction();
		
        /*try{
	        Query query = session.createQuery("select id,name,password from User u where u.id="+5);
        	//Query query = session.createSQLQuery("select * from users u where u.id="+5);
	        User user = (User)query.list().get(0);
	        System.out.println(user.getName());
			//assertTrue(new String("simon,test").equals(user.getName()+","+user.getPassword()));
	        
	        //logger.info("running learnerDao count()");
        	//HibernateDaoSupport
        	List learnerCountList = session.get  this.getHibernateTemplate().executeFind(
	        		new HibernateCallback() {
	        			public Object doInHibernate(Session session) throws HibernateException {
	        				Query query = session.createQuery("select count(*) from Learner");
	        				return query.list();
	        			}
	        		}
	        );
	        Integer learnerCount = (Integer) learnerCountList.get(0);
	        return learnerCount.intValue();
	        
	        public abstract class BaseHibernateObjectDao extends HibernateDaoSupport
			implements BaseObjectDao {
			}
	        
	        
	        
        }
        catch(Exception e){
        	e.printStackTrace();
        }
		tx.commit();
		session.close();
		*/
		//Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List<User> result = session.createQuery("from User where id="+4).list();
        session.getTransaction().commit();

        /*for(int i=0;i<result.size();i++){
        	User u = (User)result.get(i);
        	System.out.println("name:"+u.getName()+", passwd:"+u.getPassword());
        }*/
        Iterator<User> i = result.iterator();
        while (i.hasNext()) {
			User user = (User) i.next();
			System.out.println("name:"+user.getName()+", passwd:"+user.getPassword());
		}
	}
	
}
