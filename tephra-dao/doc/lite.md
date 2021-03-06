# 使用LiteOrm持久化数据
LiteOrm是Tephra为简单ORM需求提供的实现，没有复杂的映射关系，仅实现SQL与Model的映射，其目的是提供简洁、高效的ORM功能。

1、LiteQuery
```java
package org.lpw.tephra.dao.orm.lite;

import org.lpw.tephra.dao.model.Model;
import org.lpw.tephra.dao.orm.Query;
import org.lpw.tephra.dao.orm.QuerySupport;

/**
 * Lite检索构造器。用于构造非级联ORM检索语句。
 *
 * @author lpw
 */
public class LiteQuery extends QuerySupport implements Query {
    private String select;
    private String from;

    /**
     * 检索构造器。
     *
     * @param modelClass Model类。
     */
    public <T extends Model> LiteQuery(Class<T> modelClass) {
        if (modelClass == null)
            throw new NullPointerException("Model类不允许为空！");

        this.modelClass = modelClass;
    }

    /**
     * 设置数据源。
     *
     * @param dataSource 数据源。
     * @return 当前Query实例。
     */
    public LiteQuery dataSource(String dataSource) {
        this.dataSource = dataSource;

        return this;
    }

    /**
     * 设置SELECT字段集，如果为空则为所有字段。
     *
     * @param select SELECT字段集。
     * @return 当前Query实例。
     */
    public LiteQuery select(String select) {
        this.select = select;

        return this;
    }

    /**
     * 设置FROM表名称集，至少必须包含一个表名称。如果为空则使用Model类对应的表名称。
     *
     * @param from FROM表名称集。
     * @return 当前Query实例。
     */
    public LiteQuery from(String from) {
        this.from = from;

        return this;
    }

    /**
     * 设置SET片段。
     *
     * @param set SET片段。
     * @return 当前Query实例。
     */
    public LiteQuery set(String set) {
        this.set = set;

        return this;
    }

    /**
     * 设置WHERE片段。
     *
     * @param where WHERE片段。
     * @return 当前Query实例。
     */
    public LiteQuery where(String where) {
        this.where = where;

        return this;
    }

    /**
     * 设置ORDER BY片段。为空则不排序。
     *
     * @param order ORDER BY片段。
     * @return 当前Query实例。
     */
    public LiteQuery order(String order) {
        this.order = order;

        return this;
    }

    /**
     * 设置GROUP BY片段。为空则不分组。
     *
     * @param group GROUP BY片段。
     * @return 当前Query实例。
     */
    public LiteQuery group(String group) {
        this.group = group;

        return this;
    }

    /**
     * 添加悲观锁。
     *
     * @return 当前Query实例。
     */
    public LiteQuery lock() {
        locked = true;

        return this;
    }

    /**
     * 设置最大返回的记录数。如果小于1则返回全部数据。
     *
     * @param size 最大返回的记录数。
     * @return 当前Query实例。
     */
    public LiteQuery size(int size) {
        this.size = size;

        return this;
    }

    /**
     * 设置当前显示的页码。只有当size大于0时页码数才有效。如果页码小于1，则默认为1。
     *
     * @param page 当前显示的页码。
     * @return 当前Query实例。
     */
    public LiteQuery page(int page) {
        this.page = page;

        return this;
    }

    /**
     * 获取SELECT字段集。
     *
     * @return SELECT字段集。
     */
    public String getSelect() {
        return select;
    }

    /**
     * 获取FROM表名称集。
     *
     * @return FROM表名称集。
     */
    public String getFrom() {
        return from;
    }
}
```
2、LiteOrm
```java
package org.lpw.tephra.dao.orm.lite;

import org.lpw.tephra.dao.model.Model;
import org.lpw.tephra.dao.orm.Orm;

/**
 * 简单ORM。主要提供高效的ORM，但不提供自动外联合映射的功能。
 *
 * @author lpw
 */
public interface LiteOrm extends Orm<LiteQuery> {
    /**
     * 重置内存表数据。
     * 此过程会先删除内存表现有数据再导入磁盘表数据到内存表中。
     *
     * @param modelClass Model类。
     * @param count      是否检查记录数，如果设置为true并且内存表记录数与磁盘表记录数相同则不重置。
     */
    default void resetMemory(Class<? extends Model> modelClass, boolean count) {
        resetMemory(null, modelClass, count);
    }

    /**
     * 重置内存表数据。
     * 此过程会先删除内存表现有数据再导入磁盘表数据到内存表中。
     *
     * @param dataSource 数据源名称，为空则使用默认数据源。
     * @param modelClass Model类。
     * @param count      是否检查记录数，如果设置为true并且内存表记录数与磁盘表记录数相同则不重置。
     */
    void resetMemory(String dataSource, Class<? extends Model> modelClass, boolean count);
}
```
3、简单的增删改查示例：
```java
package org.lpw.tephra.dao.orm.lite;

import org.junit.Assert;
import org.junit.Test;
import org.lpw.tephra.dao.jdbc.Sql;
import org.lpw.tephra.dao.jdbc.SqlTable;
import org.lpw.tephra.dao.orm.PageList;
import org.lpw.tephra.dao.orm.TestModel;
import org.lpw.tephra.test.DaoTestSupport;
import org.lpw.tephra.util.Converter;
import org.lpw.tephra.util.TimeUnit;

import javax.inject.Inject;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author lpw
 */
public class LiteOrmTest extends DaoTestSupport {
    @Inject
    private Converter converter;
    @Inject
    private Sql sql;
    @Inject
    private LiteOrm liteOrm;

    @Test
    public void crud() {
        long time = System.currentTimeMillis();
        PageList<TestModel> pl = liteOrm.query(new LiteQuery(TestModel.class), null);
        Assert.assertNotNull(pl);
        Assert.assertEquals(0, pl.getCount());
        Assert.assertNotNull(pl.getList());
        Assert.assertTrue(pl.getList().isEmpty());

        TestModel model1 = new TestModel();
        model1.setSort(1);
        model1.setName("LiteOrm");
        model1.setDate(new Date(time - TimeUnit.Day.getTime()));
        model1.setTime(new Timestamp(time - TimeUnit.Hour.getTime()));
        liteOrm.save(model1);
        Assert.assertNotNull(model1.getId());
        Assert.assertEquals(36, model1.getId().length());

        TestModel model2 = liteOrm.findById(TestModel.class, model1.getId());
        Assert.assertNotEquals(model1.hashCode(), model2.hashCode());
        Assert.assertEquals(model1.getId(), model2.getId());
        Assert.assertEquals(1, model2.getSort());
        Assert.assertEquals("LiteOrm", model2.getName());
        Assert.assertEquals(converter.toString(new Date(time - TimeUnit.Day.getTime())), converter.toString(model2.getDate()));
        Assert.assertEquals(converter.toString(new Timestamp(time - TimeUnit.Hour.getTime())), converter.toString(model2.getTime()));

        TestModel model3 = new TestModel();
        model3.setId(model1.getId());
        model3.setName("new name");
        model3.setDate(new Date(time - 3 * TimeUnit.Day.getTime()));
        model3.setTime(new Timestamp(time - 3 * TimeUnit.Hour.getTime()));
        liteOrm.save(model3);
        TestModel model4 = liteOrm.findById(TestModel.class, model1.getId());
        Assert.assertEquals(model1.getId(), model4.getId());
        Assert.assertEquals(0, model4.getSort());
        Assert.assertEquals("new name", model4.getName());
        Assert.assertEquals(converter.toString(new Date(time - 3 * TimeUnit.Day.getTime())), converter.toString(model4.getDate()));
        Assert.assertEquals(converter.toString(new Timestamp(time - 3 * TimeUnit.Hour.getTime())), converter.toString(model4.getTime()));

        liteOrm.delete(model1);
        Assert.assertNull(liteOrm.findById(TestModel.class, model1.getId()));

        pl = liteOrm.query(new LiteQuery(TestModel.class), null);
        Assert.assertNotNull(pl);
        Assert.assertEquals(0, pl.getCount());
        Assert.assertNotNull(pl.getList());
        Assert.assertTrue(pl.getList().isEmpty());

        for (int i = 0; i < 10; i++) {
            TestModel model = new TestModel();
            model.setSort(i);
            model.setName("name" + i);
            model.setDate(new Date(time - i * TimeUnit.Day.getTime()));
            model.setTime(new Timestamp(time - i * TimeUnit.Hour.getTime()));
            liteOrm.save(model);
        }
        pl = liteOrm.query(new LiteQuery(TestModel.class).where("c_sort<?").order("c_sort"), new Object[]{5});
        Assert.assertNotNull(pl);
        Assert.assertEquals(0, pl.getCount());
        Assert.assertEquals(5, pl.getList().size());
        for (int i = 0; i < 5; i++) {
            TestModel model = pl.getList().get(i);
            Assert.assertEquals(36, model.getId().length());
            Assert.assertEquals(i, model.getSort());
            Assert.assertEquals("name" + i, model.getName());
            Assert.assertEquals(converter.toString(new Date(time - i * TimeUnit.Day.getTime())), converter.toString(model.getDate()));
            Assert.assertEquals(converter.toString(new Timestamp(time - i * TimeUnit.Hour.getTime())), converter.toString(model.getTime()));
        }

        close();
    }

    @Test
    public void memory() {
        long time = System.currentTimeMillis();
        MemoryModel model1 = new MemoryModel();
        model1.setSort(1);
        model1.setName("LiteOrm");
        model1.setDate(new Date(time - TimeUnit.Day.getTime()));
        model1.setTime(new Timestamp(time - TimeUnit.Hour.getTime()));
        liteOrm.save(model1);
        Assert.assertNotNull(model1.getId());
        Assert.assertEquals(36, model1.getId().length());
        String[] prefixes = new String[]{"t", "m"};
        for (String prefix : prefixes) {
            SqlTable table = sql.query("select * from " + prefix + "_tephra_test where c_id=?", new Object[]{model1.getId()});
            Assert.assertEquals(1, converter.toInt(table.get(0, "c_sort")));
            Assert.assertEquals("LiteOrm", table.get(0, "c_name"));
            Assert.assertEquals(converter.toString(new Date(time - TimeUnit.Day.getTime())), converter.toString(table.get(0, "c_date")));
            Assert.assertEquals(converter.toString(new Timestamp(time - TimeUnit.Hour.getTime())), converter.toString(table.get(0, "c_time")));
        }

        model1.setSort(2);
        model1.setName("name 2");
        model1.setDate(new Date(time - 2 * TimeUnit.Day.getTime()));
        model1.setTime(new Timestamp(time - 2 * TimeUnit.Hour.getTime()));
        liteOrm.save(model1);
        for (String prefix : prefixes) {
            SqlTable table = sql.query("select * from " + prefix + "_tephra_test where c_id=?", new Object[]{model1.getId()});
            Assert.assertEquals(2, converter.toInt(table.get(0, "c_sort")));
            Assert.assertEquals("name 2", table.get(0, "c_name"));
            Assert.assertEquals(converter.toString(new Date(time - 2 * TimeUnit.Day.getTime())), converter.toString(table.get(0, "c_date")));
            Assert.assertEquals(converter.toString(new Timestamp(time - 2 * TimeUnit.Hour.getTime())), converter.toString(table.get(0, "c_time")));
        }

        sql.update("update t_tephra_test set c_name=?", new Object[]{"table"});
        sql.update("update m_tephra_test set c_name=?", new Object[]{"memory"});
        SqlTable table = sql.query("select * from t_tephra_test where c_id=?", new Object[]{model1.getId()});
        Assert.assertEquals("table", table.get(0, "c_name"));
        table = sql.query("select * from m_tephra_test where c_id=?", new Object[]{model1.getId()});
        Assert.assertEquals("memory", table.get(0, "c_name"));
        MemoryModel model2 = liteOrm.findById(MemoryModel.class, model1.getId());
        Assert.assertNotNull(model2);
        Assert.assertEquals(2, model2.getSort());
        Assert.assertEquals("memory", model2.getName());

        PageList<MemoryModel> pl = liteOrm.query(new LiteQuery(MemoryModel.class), null);
        Assert.assertEquals(1, pl.getList().size());
        Assert.assertEquals(2, pl.getList().get(0).getSort());
        Assert.assertEquals("memory", pl.getList().get(0).getName());

        liteOrm.update(new LiteQuery(MemoryModel.class).set("c_sort=?,c_name=?"), new Object[]{3, "name 3"});
        table = sql.query("select * from t_tephra_test where c_id=?", new Object[]{model1.getId()});
        Assert.assertEquals(3, converter.toInt(table.get(0, "c_sort")));
        Assert.assertEquals("name 3", table.get(0, "c_name"));
        table = sql.query("select * from m_tephra_test where c_id=?", new Object[]{model1.getId()});
        Assert.assertEquals(3, converter.toInt(table.get(0, "c_sort")));
        Assert.assertEquals("name 3", table.get(0, "c_name"));

        liteOrm.delete(model1);
        table = sql.query("select * from t_tephra_test where c_id=?", new Object[]{model1.getId()});
        Assert.assertEquals(0, table.getRowCount());
        table = sql.query("select * from m_tephra_test where c_id=?", new Object[]{model1.getId()});
        Assert.assertEquals(0, table.getRowCount());

        for (int i = 0; i < 10; i++) {
            MemoryModel model = new MemoryModel();
            model.setSort(i);
            model.setName("name" + i);
            model.setDate(new Date(time - i * TimeUnit.Day.getTime()));
            model.setTime(new Timestamp(time - i * TimeUnit.Hour.getTime()));
            liteOrm.save(model);
        }
        liteOrm.delete(new LiteQuery(MemoryModel.class).where("c_sort>?"), new Object[]{4});
        for (String prefix : prefixes) {
            table = sql.query("select * from " + prefix + "_tephra_test order by c_sort", null);
            Assert.assertEquals(5, table.getRowCount());
            for (int i = 0; i < 5; i++) {
                Assert.assertEquals(i, converter.toInt(table.get(i, "c_sort")));
                Assert.assertEquals("name" + i, table.get(i, "c_name"));
                Assert.assertEquals(converter.toString(new Date(time - i * TimeUnit.Day.getTime())), converter.toString(table.get(i, "c_date")));
                Assert.assertEquals(converter.toString(new Timestamp(time - i * TimeUnit.Hour.getTime())), converter.toString(table.get(i, "c_time")));
            }
        }

        close();
    }
}
```