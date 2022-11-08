package io.github.etases.edublock.rs.entity.generator;

import org.hibernate.HibernateException;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IncrementGenerator;
import org.tinylog.Logger;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class UseExistOrIncrementGenerator extends IncrementGenerator {
    public static final String CLASS_PATH = "io.github.etases.edublock.rs.entity.generator.UseExistOrIncrementGenerator";

    private String storedSql;
    private boolean initPreviousValue = false;

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Serializable id = session.getEntityPersister(null, object).getClassMetadata().getIdentifier(object, session);
        if (id != null) {
            initPreviousValue = true;
            return id;
        }
        if (initPreviousValue) {
            callInitializePreviousValue(session);
            initPreviousValue = false;
        }
        return super.generate(session, object);
    }

    @Override
    public void initialize(SqlStringGenerationContext context) {
        super.initialize(context);
        try {
            Field sqlField = IncrementGenerator.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            storedSql = (String) sqlField.get(this);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void callInitializePreviousValue(SharedSessionContractImplementor session) {
        try {
            Field sqlField = IncrementGenerator.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(this, storedSql);

            Method initializePreviousValueMethod = getClass().getDeclaredMethod("initializePreviousValueHolder", SharedSessionContractImplementor.class);
            initializePreviousValueMethod.setAccessible(true);
            initializePreviousValueMethod.invoke(this, session);
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
