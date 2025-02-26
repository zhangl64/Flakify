/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingjdbc.common.base.AbstractEncryptJDBCDatabaseAndTableTest;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptStatementTest extends AbstractEncryptJDBCDatabaseAndTableTest {
    
    private static final String INSERT_SQL = "insert into t_encrypt(id, pwd) values(2,'b')";
    
    private static final String INSERT_GENERATED_KEY_SQL = "insert into t_encrypt(pwd) values('b')";
    
    private static final String DELETE_SQL = "delete from t_encrypt where pwd = 'a' and id = 1";
    
    private static final String UPDATE_SQL = "update t_encrypt set pwd ='f' where pwd = 'a'";
    
    private static final String SELECT_SQL = "select id, pwd from t_encrypt where pwd = 'a'";
    
    private static final String SELECT_SQL_WITH_STAR = "select * from t_encrypt where pwd = 'a'";
    
    private static final String SELECT_SQL_WITH_PLAIN = "select id, pwd from t_encrypt where pwd = 'plainValue'";
    
    private static final String SELECT_SQL_WITH_CIPHER = "select id, pwd from t_encrypt where pwd = 'plainValue'";
    
    private static final String SELECT_SQL_TO_ASSERT = "select id, cipher_pwd, plain_pwd from t_encrypt";
    
    @Test
    public void assertSqlShow() throws SQLException {
        assertTrue(getEncryptConnectionWithProps().getRuntimeContext().getProps().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW));
    }
    
    @Test
    public void assertInsertWithExecute() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            statement.execute(INSERT_SQL);
        }
        assertResultSet(3, 2, "encryptValue", "b");
    }
    
    @Test
    public void assertInsertWithExecuteWithGeneratedKey() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            statement.execute(INSERT_GENERATED_KEY_SQL, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(6));
            assertFalse(resultSet.next());
        }
        assertResultSet(3, 6, "encryptValue", "b");
    }
    
    @Test
    public void assertDeleteWithExecute() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            statement.execute(DELETE_SQL);
        }
        assertResultSet(1, 5, "encryptValue", "b");
    }
    
    @Test
    public void assertUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (Statement statement = getEncryptConnection().createStatement()) {
            result = statement.executeUpdate(UPDATE_SQL);
        }
        assertThat(result, is(2));
        assertResultSet(2, 1, "encryptValue", "f");
    }
    
    @Test
    public void assertSelectWithExecuteQuery() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL);
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(1));
            assertThat(resultSet.getString(2), is("decryptValue"));
            assertTrue(resultSet.next());
            assertThat(resultSet.getInt(1), is(5));
            assertThat(resultSet.getString(2), is("decryptValue"));
        }
    }
    
    @Test
    public void assertSelectWithExecuteWithProperties() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            int[] columnIndexes = {1, 2};
            Boolean result = statement.execute(SELECT_SQL, columnIndexes);
            assertTrue(result);
            assertThat(statement.getResultSetType(), is(ResultSet.TYPE_FORWARD_ONLY));
            assertThat(statement.getResultSetConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
            assertThat(statement.getResultSetHoldability(), is(ResultSet.HOLD_CURSORS_OVER_COMMIT));
        }
    }
    
    @Test
    public void assertSelectWithMetaData() throws SQLException {
        try (Statement statement = getEncryptConnectionWithProps().createStatement()) {
            ResultSetMetaData metaData = statement.executeQuery(SELECT_SQL_WITH_STAR).getMetaData();
            assertThat(metaData.getColumnCount(), is(3));
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                assertThat(metaData.getColumnLabel(1), is("id"));
                assertThat(metaData.getColumnLabel(2), is("cipher_pwd"));
                assertThat(metaData.getColumnLabel(3), is("pwd"));
            }
        }
    }
    
    @Test
    public void assertSelectWithCipherColumn() throws SQLException {
        try (Statement statement = getEncryptConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL_WITH_CIPHER);
            int count = 1;
            List<Object> ids = Arrays.asList((Object) 1, 5);
            while (resultSet.next()) {
                assertThat(resultSet.getObject("id"), is(ids.get(count - 1)));
                assertThat(resultSet.getObject("pwd"), is((Object) "decryptValue"));
                count += 1;
            }
            assertThat(count - 1, is(ids.size()));
        }
    }
    
    @Test
    public void assertSelectWithPlainColumn() throws SQLException {
        try (Statement statement = getEncryptConnectionWithProps().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL_WITH_PLAIN);
            int count = 1;
            List<Object> ids = Arrays.asList((Object) 1, 5);
            while (resultSet.next()) {
                assertThat(resultSet.getObject("id"), is(ids.get(count - 1)));
                assertThat(resultSet.getObject("pwd"), is((Object) "plainValue"));
                count += 1;
            }
            assertThat(count - 1, is(ids.size()));
        }
    }
    
    private void assertResultSet(final int resultSetCount, final int id, final Object pwd, final Object plain) throws SQLException {
        try (Connection conn = getDatabaseTypeMap().values().iterator().next().values().iterator().next().getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(SELECT_SQL_TO_ASSERT);
            int count = 1;
            while (resultSet.next()) {
                if (id == count) {
                    assertThat(resultSet.getObject("cipher_pwd"), is(pwd));
                    assertThat(resultSet.getObject("plain_pwd"), is(plain));
                }
                count += 1;
            }
            assertThat(count - 1, is(resultSetCount));
        }
    }
}
