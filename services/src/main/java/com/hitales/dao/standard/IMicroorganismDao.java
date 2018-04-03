package com.hitales.dao.standard;

import com.hitales.dao.TableDao;
import com.hitales.entity.Microorganism;

import java.util.List;

public interface IMicroorganismDao extends TableDao<Microorganism> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

    String findPatientIdByGroupRecordName(String dataSource, String applyId);
}