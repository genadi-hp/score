/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.engine.node.repositories;

import io.cloudslang.score.api.nodes.WorkerStatus;
import io.cloudslang.engine.node.entities.WorkerNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 11/11/12
 */
public interface WorkerNodeRepository extends JpaRepository<WorkerNode,Long> {
	@Modifying
	@Query("delete from WorkerNode w where w.uuid = ?1")
	void deleteByUuid(String uuid);

    WorkerNode findByUuidAndDeleted(String uuid, boolean deleted);

    WorkerNode findByUuid(String uuid);

    List<WorkerNode> findByActiveAndDeleted(boolean active, boolean deleted);

    List<WorkerNode> findByDeletedOrderByIdAsc(boolean deleted);

	List<WorkerNode> findByActiveAndStatusAndDeleted(boolean isActive, WorkerStatus status, boolean deleted);

	List<WorkerNode> findByActiveAndStatusAndDeletedAndVersionId(boolean isActive, WorkerStatus status, boolean deleted, String versionId);

	List<WorkerNode> findByGroupsAndDeleted(String group, boolean deleted);

	@Query("select w.uuid from WorkerNode w where (w.ackVersion < ?1) and w.status <> ?2")
	List<String> findNonRespondingWorkers(long ackVersion, WorkerStatus status);

	@Query("select distinct g from WorkerNode w join w.groups g where w.deleted = false")
	List<String> findGroups();

	@Query(value = "update WorkerNode w set w.ackTime = current_time where w.uuid = ?1")
	@Modifying
	void updateAckTime(String uuid);


	@Query("select distinct g from WorkerNode w join w.groups g where g in ?1")
	List<String> findGroups(List<String> groupName);

	@Modifying @Query("update WorkerNode w set w.uuid = w.uuid where w.uuid = ?1")
	void lockByUuid(String uuid);
}