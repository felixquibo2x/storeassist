package storeassist.kwibs.com.storeassist.database.dao;

import java.util.List;

/**
 * Created by cits-kwibs on 23/01/2018.
 */

public interface Dao <Q> {
    public void add(Q q) throws Exception;
    public void update(Q q) throws Exception;
    public void delete(Q q) throws Exception;
    public List<Q> findAll() throws Exception;
}
