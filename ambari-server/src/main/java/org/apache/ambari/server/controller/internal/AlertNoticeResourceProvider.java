/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.controller.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.controller.AlertNoticeRequest;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.RequestStatus;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.orm.dao.AlertDispatchDAO;
import org.apache.ambari.server.orm.entities.AlertDefinitionEntity;
import org.apache.ambari.server.orm.entities.AlertHistoryEntity;
import org.apache.ambari.server.orm.entities.AlertNoticeEntity;
import org.apache.ambari.server.orm.entities.AlertTargetEntity;
import org.apache.ambari.server.orm.entities.ClusterEntity;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * ResourceProvider for Alert History
 */
public class AlertNoticeResourceProvider extends AbstractResourceProvider {

  public static final String ALERT_HISTORY = "AlertNotice";
  public static final String ALERT_NOTICE_ID = "AlertNotice/id";
  public static final String ALERT_NOTICE_STATE = "AlertNotice/notification_state";
  public static final String ALERT_NOTICE_UUID = "AlertNotice/uuid";
  public static final String ALERT_NOTICE_SERVICE_NAME = "AlertNotice/service_name";
  public static final String ALERT_NOTICE_TARGET_ID = "AlertNotice/target_id";
  public static final String ALERT_NOTICE_TARGET_NAME = "AlertNotice/target_name";
  public static final String ALERT_NOTICE_HISTORY_ID = "AlertNotice/history_id";
  public static final String ALERT_NOTICE_CLUSTER_NAME = "AlertNotice/cluster_name";

  private static final Set<String> PK_PROPERTY_IDS = new HashSet<String>(
      Arrays.asList(ALERT_NOTICE_ID));

  /**
   * Used for querying alert history.
   */
  private static AlertDispatchDAO s_dao = null;

  /**
   * The property ids for an alert history resource.
   */
  private static final Set<String> PROPERTY_IDS = new HashSet<String>();

  /**
   * The key property ids for an alert history resource.
   */
  private static final Map<Resource.Type, String> KEY_PROPERTY_IDS =
      new HashMap<Resource.Type, String>();

  static {
    // properties
    PROPERTY_IDS.add(ALERT_NOTICE_ID);
    PROPERTY_IDS.add(ALERT_NOTICE_STATE);
    PROPERTY_IDS.add(ALERT_NOTICE_UUID);
    PROPERTY_IDS.add(ALERT_NOTICE_SERVICE_NAME);
    PROPERTY_IDS.add(ALERT_NOTICE_TARGET_ID);
    PROPERTY_IDS.add(ALERT_NOTICE_TARGET_NAME);
    PROPERTY_IDS.add(ALERT_NOTICE_HISTORY_ID);
    PROPERTY_IDS.add(ALERT_NOTICE_CLUSTER_NAME);

    // keys
    KEY_PROPERTY_IDS.put(Resource.Type.AlertNotice, ALERT_NOTICE_ID);
    KEY_PROPERTY_IDS.put(Resource.Type.Cluster, ALERT_NOTICE_CLUSTER_NAME);
  }

  /**
   * Static intializer for Guice.
   *
   * @param instance
   */
  @Inject
  public static void init(Injector injector) {
    s_dao = injector.getInstance(AlertDispatchDAO.class);
  }

  /**
   * Constructor.
   *
   * @param controller
   */
  AlertNoticeResourceProvider() {
    super(PROPERTY_IDS, KEY_PROPERTY_IDS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Set<String> getPKPropertyIds() {
    return PK_PROPERTY_IDS;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RequestStatus createResources(Request request) throws SystemException,
      UnsupportedPropertyException, ResourceAlreadyExistsException,
      NoSuchParentResourceException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RequestStatus updateResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RequestStatus deleteResources(Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    Set<String> requestPropertyIds = getRequestPropertyIds(request, predicate);
    Set<Resource> results = new LinkedHashSet<Resource>();

    Request.PageInfo pageInfo = request.getPageInfo();
    Request.SortInfo sortInfo = request.getSortInfo();

    AlertNoticeRequest noticeRequest = new AlertNoticeRequest();
    noticeRequest.Predicate = predicate;

    if (null != pageInfo) {
      noticeRequest.Pagination = pageInfo.getPageRequest();

      pageInfo.setResponsePaged(true);
      pageInfo.setTotalCount(s_dao.getNoticesCount(predicate));
    }

    if (null != sortInfo) {
      sortInfo.setResponseSorted(true);
      noticeRequest.Sort = sortInfo.getSortRequest();
    }

    List<AlertNoticeEntity> entities = s_dao.findAllNotices(noticeRequest);
    for (AlertNoticeEntity entity : entities) {
      results.add(toResource(entity, requestPropertyIds));
    }

    return results;
  }

  /**
   * Converts the {@link AlertNoticeEntity} to a {@link Resource}.
   *
   * @param clusterName
   *          the name of the cluster (not {@code null}).
   * @param entity
   *          the entity to convert (not {@code null}).
   * @param requestedIds
   *          the properties requested (not {@code null}).
   * @return
   */
  private Resource toResource(AlertNoticeEntity entity,
      Set<String> requestedIds) {
    AlertHistoryEntity history = entity.getAlertHistory();
    AlertTargetEntity target = entity.getAlertTarget();
    AlertDefinitionEntity definition = history.getAlertDefinition();
    ClusterEntity cluster = definition.getCluster();

    Resource resource = new ResourceImpl(Resource.Type.AlertNotice);
    resource.setProperty(ALERT_NOTICE_ID, entity.getNotificationId());

    setResourceProperty(resource, ALERT_NOTICE_STATE, entity.getNotifyState(),
        requestedIds);

    setResourceProperty(resource, ALERT_NOTICE_UUID, entity.getUuid(),
        requestedIds);

    setResourceProperty(resource, ALERT_NOTICE_SERVICE_NAME,
        definition.getServiceName(), requestedIds);

    setResourceProperty(resource, ALERT_NOTICE_TARGET_ID, target.getTargetId(),
        requestedIds);

    setResourceProperty(resource, ALERT_NOTICE_TARGET_NAME,
        target.getTargetName(), requestedIds);

    setResourceProperty(resource, ALERT_NOTICE_HISTORY_ID,
        history.getAlertId(), requestedIds);

    if (null != cluster) {
      setResourceProperty(resource, ALERT_NOTICE_CLUSTER_NAME,
          cluster.getClusterName(), requestedIds);
    }

    return resource;
  }
}
