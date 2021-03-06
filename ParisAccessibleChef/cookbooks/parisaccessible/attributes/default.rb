

node.default["parisaccessible"]["home"] = "/srv/ParisAccessible" 
node.default["parisaccessible"]["repository"] = "git://github.com/zanni/ParisAccessible.git"
node.default["parisaccessible"]["jest_repository"] = "git://github.com/searchbox-io/Jest.git"
node.default["parisaccessible"]["neo_spatial_repository"] = "git://github.com/neo4j-contrib/spatial.git"

node.default["parisaccessible"]["log"] = "/var/log/parisaccessible"
node.default["parisaccessible"]["ratp_gtfs_url"] = "http://dataratp.download.opendatasoft.com/RATP_GTFS_FULL.zip"
node.default["parisaccessible"]["accessibility_trottoir_url"] = "http://opendata.paris.fr/explore/dataset/trottoirs_des_rues_de_paris/download/?format=csv"
node.default["parisaccessible"]["accessibility_passagepieton_url"] = "http://opendata.paris.fr/explore/dataset/mobiliers_et_emprises_au_sol_de_signalisation_routiere_et_pietonne_-_donnees_geo/download/?format=csv"
node.default["parisaccessible"]["accessibility_equipement_url"] = "http://opendata.paris.fr/explore/dataset/accessibilite_des_equipements_de_la_ville_de_paris/download/?format=csv"

node.default['parisaccessible']['snapshot']['region'] = "us-east-1"
node.default['parisaccessible']['snapshot']['bucket'] = "bzanni"


node.default["parisaccessible"]["elasticsearch_url"] = "http://192.168.33.10:9200"
node.default["parisaccessible"]["neo4j_url"] = "http://localhost:7474/db/data"
node.default["parisaccessible"]["neo4j_data_path"] = "/var/lib/neo4j-server/data/"

node.default["parisaccessible"]["parisaccessible_app_datafolder"] = "graph.db"
node.default["parisaccessible"]["inject_path"] = "/srv/ParisAccessible/inject/"

node.default["parisaccessible"]["rabbitmq_host"]  = "localhost"
node.default["parisaccessible"]["rabbitmq_port"]  = 5672
node.default["parisaccessible"]["rabbitmq_username"]  = "bzanni"
node.default["parisaccessible"]["rabbitmq_password"]  = "bzanni"

node.default["parisaccessible"]["memcached_url"]  = "localhost"
node.default["parisaccessible"]["memcached_port"]  = 11211

node.default["parisaccessible"]["ratp_gtfs_index_name"] = "ratp_gtfs"
node.default["parisaccessible"]["accesibility_index_name"] = "accessibility"

node.default["parisaccessible"]["gtfs_trip_filename"] = "trips.txt"
node.default["parisaccessible"]["gtfs_service_filename"]  = "calendar.txt"
node.default["parisaccessible"]["gtfs_service_calendar_filename"]  = "calendar_dates.txt"
node.default["parisaccessible"]["gtfs_route_filename"]  = "routes.txt"
node.default["parisaccessible"]["gtfs_stop_filename"]  = "stops.txt"
node.default["parisaccessible"]["gtfs_stoptime_filename"]  = "stop_times.txt"
node.default["parisaccessible"]["gtfs_transfert_filename"] = "transfers.txt"
node.default["parisaccessible"]["gtfs_agency_filename"]  = "agency.txt"

node.default["parisaccessible"]["accessibility_opendataparis_trottoir_filename"]  = "trottoir.csv"
node.default["parisaccessible"]["accessibility_opendataparis_equipement_filename"]  = "equipement.csv"
node.default["parisaccessible"]["accessibility_opendataparis_passagepieton_filename"]  = "passagepieton.csv"
node.default["parisaccessible"]["accessibility_opendataratp_route_filename"]  = "route_access.csv"
node.default["parisaccessible"]["accessibility_opendataratp_stop_filename"]  = "stop_access.csv"

node.default["parisaccessible"]["index_cost_pieton_speed"] = 5
node.default["parisaccessible"]["index_cost_trottoir_speed"] = 3

node.default["parisaccessible"]["index_match_trottoir_passagepieton_distance"] = 5
node.default["parisaccessible"]["index_match_trottoir_stop_distance"] = 5

mail = Chef::DataBagItem.load('parisaccessible', 'mail')['_default'] rescue {}
mail.each do |k, v|
	node.default["parisaccessible"][k] = v
end

allocated_memory = "#{(node.memory.total.to_i * 0.8 ).floor / 1024}m"
node.default[:elasticsearch][:allocated_memory] = allocated_memory



