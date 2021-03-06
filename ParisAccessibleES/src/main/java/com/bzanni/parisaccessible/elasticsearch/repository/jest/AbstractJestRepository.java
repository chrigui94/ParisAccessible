package com.bzanni.parisaccessible.elasticsearch.repository.jest;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.bzanni.parisaccessible.elasticsearch.business.JestBusiness;
import com.google.gson.JsonObject;

public abstract class AbstractJestRepository<T extends JestBusiness> {

	private final static int MAX_RETRY = 3;
	@Resource
	private ParisAccessibleJestClient client;
	@Resource
	private JestQueryEngine queryEngine;

	public abstract String getIndex();

	public abstract String getType();

	private Class<T> klass;

	// @PostConstruct
	// public void initilization() {
	// client = new ParisAccessibleJestClient(host);
	// }

	@PreDestroy
	public void destroy() {
		if (client != null) {
			client.shutdownClient();
		}
	}

	protected boolean mappings(Class<T> k, int shards, int replicas)
			throws Exception {

		klass = k;

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("number_of_shards", shards);
		jsonObject.addProperty("number_of_replicas", replicas);

		JestResult execute;
		execute = client.getClient().execute(
				new CreateIndex.Builder(this.getIndex()).settings(jsonObject)
						.build());

		PutMapping putMapping = new PutMapping.Builder(this.getIndex(),
				this.getType(), queryEngine.putMappingQuery(k)).build();

		execute = client.getClient().execute(putMapping);

		return execute.isSucceeded();
	}

	public Iterator<List<T>> findAll() {
		return new JestRequestIterator<T>(this, klass);
	}

	public Iterator<List<T>> findAll(String query) {
		return new JestRequestIterator<T>(this, klass, query);
	}

	public Iterator<List<T>> findAllWorker(int index_worker, int total_worker) {
		return new JestRequestIterator<T>(this, klass, index_worker,
				total_worker);
	}

	public Iterator<List<T>> findAllWorker(int index_worker, int total_worker,
			String query) {
		return new JestRequestIterator<T>(this, klass, index_worker,
				total_worker, query);
	}

	public void save(T object) throws Exception {
		Index index = new Index.Builder(object).index(this.getIndex())
				.type(this.getType()).build();
		client.getClient().execute(index);
	}

	public void save(List<T> object) throws Exception {
		this.save(object, 0);
	}

	public void save(List<T> object, int retry) throws Exception {
		Builder builder = new Bulk.Builder();
		for (T t : object) {
			builder.addAction(new Index.Builder(t).refresh(false)
					.index(this.getIndex()).type(this.getType()).build());
		}

		try {
			client.getClient().execute(builder.build());
		} catch (SocketTimeoutException e) {
			// retry++;
			// if(retry < AbstractJestRepository.MAX_RETRY){
			// client.getClient().execute(builder.build());
			// }
		}
	}

	public void saveAsync(List<T> object, JestResultHandler<JestResult> callback)
			throws ExecutionException, InterruptedException, IOException {
		Builder builder = new Bulk.Builder();
		for (T t : object) {
			builder.addAction(new Index.Builder(t).refresh(false)
					.index(this.getIndex()).type(this.getType()).build());
		}

		client.getClient().executeAsync(builder.build(), callback);

	}

	public T findById(String id) throws Exception {
		Get get = new Get.Builder(this.getIndex(), id).type(this.getType())
				.build();

		JestResult result = client.getClient().execute(get);

		return result.getSourceAsObject(klass);
	}

	public T prepare(String id) throws Exception {
		T object = this.findById(id);
		if (object == null) {
			object = klass.newInstance();
			object.setId(id);
		}
		return object;
	}

	public JestClient getClient() {
		return client.getClient();
	}
}
