package com.hitales.dao.standard;

import java.util.List;

/**
 * @author aron
 */
@Deprecated
public interface IPathologyDao<T> extends TextDao<T> {

    List<String> findOrgOdCatByGroupRecordName(String dataSource, String groupRecordName);

}
