/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.lens.server.query.collect;

import java.util.*;

import org.apache.lens.server.api.query.QueryContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiValueMap;

import com.google.common.collect.Sets;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation which creates multiple in memory views of queries existing in lens system and owns responsibility of
 * keeping all the views consistent with each other. This implementation is not thread-safe.
 *
 * @see QueryCollection
 */
@NoArgsConstructor
@Slf4j
public class DefaultQueryCollection implements QueryCollection {

  private final Set<QueryContext> queries = Sets.newLinkedHashSet();
  private final MultiValueMap queriesByUser = MultiValueMap.decorate(new HashMap(), LinkedHashSet.class);

  public DefaultQueryCollection(@NonNull final Set<QueryContext> queries) {
    addAll(queries);
  }

  @Override
  public boolean add(final QueryContext query) {

    queriesByUser.put(query.getSubmittedUser(), query);
    return queries.add(query);
  }

  @Override
  public boolean addAll(final Set<QueryContext> queries) {

    boolean modified = false;
    for (QueryContext query : queries) {
      modified |= add(query);
    }
    return modified;
  }

  @Override
  public boolean remove(final QueryContext query) {
    queriesByUser.remove(query.getSubmittedUser(), query);
    return queries.remove(query);
  }

  @Override
  public boolean removeAll(Set<QueryContext> queries) {

    boolean modified = false;
    for (QueryContext query : queries) {
      modified |= remove(query);
    }
    return modified;
  }

  @Override
  public Set<QueryContext> getQueries() {
    return Sets.newHashSet(queries);
  }

  @Override
  public Set<QueryContext> getQueries(final String user) {
    final Collection<QueryContext> userQueries = getQueriesCollectionForUser(user);
    return Sets.newLinkedHashSet(userQueries);
  }

  @Override
  public int getQueriesCount() {
    return queries.size();
  }

  private Collection<QueryContext> getQueriesCollectionForUser(final String user) {

    final Collection<QueryContext> userQueries = queriesByUser.getCollection(user);
    return userQueries != null ? userQueries : CollectionUtils.EMPTY_COLLECTION;
  }
}
