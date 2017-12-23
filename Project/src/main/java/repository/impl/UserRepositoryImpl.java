package repository.impl;

import db.DataBaseNames;
import model.user.User;
import model.user.UserRole;
import repository.Repository;
import specifications.factory.SpecificationFactory;
import specifications.sql.SqlSpecification;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by niict on 23.12.2017.
 */
public class UserRepositoryImpl implements Repository<User> {
    @Inject
    private SpecificationFactory specificationFactory;
    @Inject
    private DataBaseHelper dataBaseHelper;
    private List<String> neededSelectTableColumns;

    public UserRepositoryImpl(){
        neededSelectTableColumns = Arrays.asList(DataBaseNames.USERS + ".USER_ID",
                DataBaseNames.USERS + ".LOGIN",
                DataBaseNames.USERS + ".PASSWORD",
                DataBaseNames.USERS + ".ROLE_ID");
    }

    @Override
    public void add(User item) {
        try {
            item.setUserID(generateUserId());
            String sql = getInsertSqlForUser(item);
            dataBaseHelper.executeUpdateQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(Iterable<User> items) {
        for (User user : items){
            add(user);
        }
    }

    @Override
    public void update(User item) {
        try {
            String sql = getUpdateSqlForUser(item);
            dataBaseHelper.executeUpdateQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(User item) {
        SqlSpecification specification = (SqlSpecification) specificationFactory.getUserByIdSpecification(item.getUserID());
        remove(specification);
    }

    @Override
    public void remove(SqlSpecification sqlSpecification) {
        try {
            String sql = dataBaseHelper.buildDeleteQueryBySQLSpecification(sqlSpecification);
            dataBaseHelper.executeUpdateQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> query(SqlSpecification sqlSpecification) {
        try {
            String sql = dataBaseHelper.buildSelectQueryBySQLSpecification(neededSelectTableColumns, sqlSpecification);
            List<User> users = executeSelect(sql);
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long generateUserId() throws SQLException {
        return Long.valueOf(dataBaseHelper.getNextValueForSequence(DataBaseNames.USER_ID_SEQUENCE));
    }

    private String getInsertSqlForUser(User user){
        ObjectColumnValues objectColumnValues = getObjectColumnValuesForUser(user);
        String sql = dataBaseHelper.buildInsertQuery(DataBaseNames.USERS, objectColumnValues);
        return sql;
    }

    private String getUpdateSqlForUser(User user){
        ObjectColumnValues objectColumnValues = getObjectColumnValuesForUser(user);
        String sql = dataBaseHelper.buildUpdateQuery(DataBaseNames.USERS, objectColumnValues);
        return sql;
    }

    private List<User> executeSelect(String sql) throws SQLException {
        ResultSet resultSet = dataBaseHelper.executeSelectQuery(sql);
        List<User> users = parseResultSet(resultSet);
        resultSet.close();
        return users;
    }

    private List<User> parseResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<User> users = new ArrayList<User>();
        while (resultSet.next()) {
            long userId = resultSet.getLong("USER_ID");
            String login = resultSet.getString("LOGIN");
            String password = resultSet.getString("PASSWORD");
            long roleId = resultSet.getLong("ROLE_ID");
            UserRole role = UserRole.getRoleByRoleId(roleId);
            User user = new User(userId, login, password, role);
            users.add(user);
        }
        return users;
    }

    private ObjectColumnValues getObjectColumnValuesForUser(User user){
        ObjectColumnValues objectColumnValues = new ObjectColumnValues();
        objectColumnValues.setValueByColumnName("User_id", String.valueOf(user.getUserID()));
        objectColumnValues.setValueByColumnName("LOGIN", String.valueOf(user.getLogin()));
        objectColumnValues.setValueByColumnName("PASSWORD", String.valueOf(user.getPassword()));
        objectColumnValues.setValueByColumnName("ROLE_ID", String.valueOf(user.getUserRole().getRoleID()));
        objectColumnValues.setIdColumnName("USER_ID");
        objectColumnValues.setObjectId(String.valueOf(user.getUserID()));
        return objectColumnValues;
    }
}