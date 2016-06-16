/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ec2box.manage.util;

import org.apache.commons.dbcp.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ec2box.common.util.AppConfig;

/**
 * Class to create a pooling data source object using commons DBCP
 *
 */
public class DSPool {

    private static Logger log = LoggerFactory.getLogger(DSPool.class);

    private static PoolingDataSource dsPool;

    private static String DB_PATH = AppConfig.getProperty("dbPath");
    private static int MAX_ACTIVE = Integer.parseInt(AppConfig.getProperty("maxActive"));
    private static boolean TEST_ON_BORROW = Boolean.valueOf(AppConfig.getProperty("testOnBorrow"));
    private static  int MIN_IDLE = Integer.parseInt(AppConfig.getProperty("minIdle"));
    private static int MAX_WAIT = Integer.parseInt(AppConfig.getProperty("maxWait"));

    private DSPool() {
    }


    /**
     * fetches the data source for H2 db
     *
     * @return data source pool
     */

    public static org.apache.commons.dbcp.PoolingDataSource getDataSource() {
        if (dsPool == null) {

            dsPool = registerDataSource();
        }
        return dsPool;

    }

    /**
     * register the data source for H2 DB
     *
     * @return pooling database object
     */

    private static PoolingDataSource registerDataSource() {


        // create a database connection
        String user="ec2box";
        String password="filepwd 0WJLnwhpA47EepT1A4drVnDn3vYRvJhpZi0sVdvN9SmlbKw";
        String connectionURI = "jdbc:h2:" + getDBPath() + "/ec2box;CIPHER=AES";

        String validationQuery = "select 1";

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            log.error(ex.toString(), ex);
        }


        GenericObjectPool connectionPool = new GenericObjectPool(null);

        connectionPool.setMaxActive(MAX_ACTIVE);
        connectionPool.setTestOnBorrow(TEST_ON_BORROW);
        connectionPool.setMinIdle(MIN_IDLE);
        connectionPool.setMaxWait(MAX_WAIT);
        connectionPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);


        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionURI, user, password);


        new PoolableConnectionFactory(connectionFactory, connectionPool, null, validationQuery, false, true);

        return new PoolingDataSource(connectionPool);

    }

    private static String getDBPath() {
        if(StringUtils.isEmpty(DB_PATH)){
            //system path to the H2 DB
            return DBUtils.class.getClassLoader().getResource("ec2db").getPath();
        }
        return DB_PATH;
    }

}

