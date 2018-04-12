package com.hitales.dao.shtr;

import com.alibaba.fastjson.JSONObject;
import com.hitales.common.constant.CommonConstant;
import com.hitales.dao.BaseDao;
import com.hitales.dao.standard.IAssayDao;
import com.hitales.entity.Assay;
import com.hitales.entity.AssayApply;
import com.hitales.entity.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository("shtrAssayDao")
public class SHTRAssayDaoImpl extends BaseDao implements IAssayDao {

    @Override
    protected String generateQuerySql() {
        return "select patientId,groupRecordName,itemTime from shtr_lab_20180402 group by groupRecordName,itemTime";
    }

    @Override
    protected <T> RowMapper<T> generateRowMapper() {
        if (getRowMapper() == null) {
            setRowMapper(new AssayRowMapper());
        }
        return getRowMapper();
    }

    @Override
    public List<Record> findRecord(String dataSource, int pageNum, int pageSize) {
        return super.queryForList(getJdbcTemplate(dataSource), pageNum, pageSize);
    }

    @Override
    public List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName) {
        /*String sql = "select t.Diagnosis from Diagnosis t where t.groupRecordName= ? group by t.Diagnosis";
        return super.findOrgOdCatByGroupRecordName(sql, dataSource, groupRecordName);*/
        return null;
    }

    @Override
    public void batchInsert2HRS(List<JSONObject> records, String collectionName) {
        synchronized (this) {
            hrsMongoTemplate.insert(records, collectionName);
        }
    }

    @Override
    public Integer getCount(String dataSource) {
        return getJdbcTemplate(dataSource).queryForObject("select count(t.groupRecordName) from (select groupRecordName from shtr_lab_20180402 group by groupRecordName,itemTime) t", Integer.class);
    }

    @Override
    public List<Assay> findArrayListByCondition(String dataSource, String... params) {
        log.debug("findArrayListByCondition(): condition: " + params[0] + "," + params[1]);
        String sql = "select patientId AS 'patientId', itemName AS 'assayName', itemTime AS 'assayTime',itemResultCode AS 'resultFlag',itemResultName AS 'assayResult',itemResultNum AS 'assayValue',itemUnit AS 'assayUnit',resultRefrence AS 'referenceRange' from shtr_lab_20180402 where groupRecordName =? and itemTime=?";
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dataSource);
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(Assay.class), params[0], params[1]);
    }

    @Override
    public JSONObject findRecordByIdInHRS(String applyId) {
        return null;
    }

    @Override
    public String findPatientIdByGroupRecordName(String dataSource, String applyId) {
        return null;
    }

    @Override
    public List<AssayApply> findBasicArrayByCondition(String dataSource, String applyId) {
        return null;
    }

    class AssayRowMapper implements RowMapper<Record> {

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            Record record = new Record();

            String groupRecordName = rs.getString("groupRecordName");

            record.setPatientId(rs.getString("patientId"));
            record.setSourceId(groupRecordName);
            record.setGroupRecordName(groupRecordName);
            record.setReportDate(rs.getString("itemTime"));
            return record;
        }
    }

}