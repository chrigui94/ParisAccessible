package com.bzanni.parisaccessible.neo.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.DefaultFileSystemAbstraction;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bzanni.parisaccessible.neo.business.Location;
import com.bzanni.parisaccessible.neo.business.Path;

@Service
@Configurable
public class BatchInserterService {

	private final static long LONG_BULK = 100;

	@Value("${neo4j_data_path}")
	private String neoDataPath;

	@Resource
	private MemcachedService cache;

	private BatchInserter inserter;
	private BatchInserterIndexProvider indexProvider;

	private long nodes = 0;
	private long relationships = 0;

	// public void deleteFolder(File folder) {
	// File[] files = folder.listFiles();
	// if (files != null) { // some JVMs return null for empty dirs
	// for (File f : files) {
	// if (f.isDirectory()) {
	// deleteFolder(f);
	// } else {
	// f.delete();
	// }
	// }
	// }
	// folder.delete();
	// }

	@PostConstruct
	public void init() {
		if (inserter == null) {
			Map<String, String> config = new HashMap<>();
			config.put("neostore.nodestore.db.mapped_memory", "90M");
			config.put("neostore.relationshipstore.db.mapped_memory", "90M");
			config.put("neostore.propertystore.db.mapped_memory", "90M");
			config.put("neostore.propertystore.db.strings.mapped_memory",
					"200M");
			config.put("neostore.propertystore.db.arrays.mapped_memory", "200M");

			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HH:mm:ss");
			String folder = neoDataPath + "/" + format.format(new Date())
					+ "_batch.db";

			// File file = new File(folder);
			// if (file.exists() && file.isDirectory()) {
			// deleteFolder(file);
			//
			// }

			inserter = BatchInserters.inserter(folder,
					new DefaultFileSystemAbstraction(), config);

			// inserter.createDeferredSchemaIndex(TrottoirIndexerService.locationLabel)
			// .on("id").create();

			indexProvider = new LuceneBatchInserterIndexProvider(inserter);
		}
	}

	@PreDestroy
	public void destroy() {
		this.flushAndShutdown();
	}

	public Long addLocationToInserter(Location location) {

		long createNode = inserter.createNode(location.getMap(),
				DynamicLabel.label(location.getLabel()));
		// System.out.println("Create " + location.getLabel() + ": " +
		// createNode);
		location.setGraphId(createNode);

		cache.set(location.getId(), location);
		setNodes(getNodes() + 1);
		if (getNodes() % BatchInserterService.LONG_BULK == 0) {
			System.out.println("Nodes: " + getNodes());
		}
		return createNode;
	}

	public void addBidirectionalToInserter(List<? extends Path> list) {
		for (Path p : list) {
			addBidirectionalToInserter(p);
		}
	}

	public void addBidirectionalToInserter(Path path) {
		Long start = path.getStart().getGraphId();
		Long end = path.getEnd().getGraphId();
		if (start == null) {
			String id = path.getStart().getId();
			Location s = (Location) cache.get(id);
			if (s != null && s.getGraphId() != null) {
				start = s.getGraphId();
			} else {
				start = this.addLocationToInserter(path.getStart());
			}
		}
		if (end == null) {
			String id = path.getEnd().getId();
			Location s = (Location) cache.get(id);
			if (s != null && s.getGraphId() != null) {
				end = s.getGraphId();
			} else {
				end = this.addLocationToInserter(path.getEnd());
			}
		}

		if (start != null && end != null) {
			inserter.createRelationship(start, end,
					DynamicRelationshipType.withName(path.getType()),
					path.getMap());

			setRelationships(getRelationships() + 1);
			if (getNodes() % BatchInserterService.LONG_BULK == 0) {
				System.out.println("Relationships: " + getRelationships());
			}

			inserter.createRelationship(end, start,
					DynamicRelationshipType.withName(path.getType()),
					path.getMap());

			setRelationships(getRelationships() + 1);
			if (getNodes() % BatchInserterService.LONG_BULK == 0) {
				System.out.println("Relationships: " + getRelationships());
			}
		}

	}

	public void flushAndShutdown() {
		indexProvider.shutdown();
		inserter.shutdown();
	}

	public long getNodes() {
		return nodes;
	}

	private void setNodes(long nodes) {
		this.nodes = nodes;
	}

	public long getRelationships() {
		return relationships;
	}

	private void setRelationships(long relationships) {
		this.relationships = relationships;
	}

}
