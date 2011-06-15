package model;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.HibernateUtil;

public class UserDao {
	private Session session;
    private Transaction tx;
	
    public User find(long id){
    	User user = null;
    	try{
    		session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
    		user = (User)session.load(User.class,id);
    	} catch (HibernateException e) {
    		tx.rollback();
        } finally {
            session.close();
        }
        return user;
    }
}
