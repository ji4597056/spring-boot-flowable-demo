#################################################
# container setting

TOMCAT_IMAGE_VERSION = 8.0-alpine
FLOWABLE_VERSION = 6.2.1
CONTAINER_NAME = flowable-all
CONTAINER_PORT = 8080
CONTAINER_HOST = 172.24.4.190

LOG_PATH = /tmp/data

# flowable setting

DATASOURCE_DRIVER = com.mysql.jdbc.Driver
DATASOURCE_URL = "jdbc:mysql://172.24.4.99:3306/flowable?characterEncoding=UTF-8&useSSL=false"
DATASOURCE_USERNAME = root
DATASOURCE_PASSWORD = root

ADMIN_USERID = admin
ADMIN_PASSWORD = admin

#################################################

all: run

build: Dockerfile.tmplt rm
	docker pull tomcat:$(TOMCAT_IMAGE_VERSION)
	@sed "s/TOMCAT_IMAGE_VERSION/$(TOMCAT_IMAGE_VERSION)/g" Dockerfile.tmplt > Dockerfile
	docker build -t flowable-all:v$(FLOWABLE_VERSION) .
	@rm -v Dockerfile

run: rm
	docker run -d -p $(CONTAINER_PORT):8080 \
		--name $(CONTAINER_NAME) \
		--user root \
		--log-opt max-size=10m \
		--log-opt max-file=5 \
		--restart always \
		-e datasource.driver=$(DATASOURCE_DRIVER) \
		-e datasource.url=$(DATASOURCE_URL) \
		-e datasource.username=$(DATASOURCE_USERNAME) \
		-e datasource.password=$(DATASOURCE_PASSWORD) \
		-e admin.userid=$(ADMIN_USERID) \
		-e admin.password=$(ADMIN_PASSWORD) \
		-e idm.app.url=http://$(CONTAINER_HOST):$(CONTAINER_PORT)/flowable-idm \
		-e idm.admin.user=$(ADMIN_USERID) \
		-e idm.admin.password=$(ADMIN_PASSWORD) \
		-e contentstorage.fs.rootFolder=/tmp/data/ \
		-e contentstorage.fs.createRoot=true \
		-e debugger.enabled=false \
		-e deployment.api.url=http://$(CONTAINER_HOST):$(CONTAINER_PORT)/flowable-task/process-api \
		-e rest.process.app.host=http://$(CONTAINER_HOST) \
		-e rest.process.app.port=$(CONTAINER_PORT) \
		-e rest.process.app.user=$(ADMIN_USERID) \
		-e rest.process.app.password=$(ADMIN_PASSWORD) \
		-e rest.dmn.app.host=http://$(CONTAINER_HOST) \
		-e rest.dmn.app.port=$(CONTAINER_PORT) \
		-e rest.dmn.app.user=$(ADMIN_USERID) \
		-e rest.dmn.app.password=$(ADMIN_PASSWORD) \
		-e rest.form.app.host=http://$(CONTAINER_HOST) \
		-e rest.form.app.port=$(CONTAINER_PORT) \
		-e rest.form.app.user=$(ADMIN_USERID) \
		-e rest.form.app.password=$(ADMIN_PASSWORD) \
		-e rest.content.app.host=http://$(CONTAINER_HOST) \
		-e rest.content.app.port=$(CONTAINER_PORT) \
		-e rest.content.app.user=$(ADMIN_USERID) \
		-e rest.content.app.password=$(ADMIN_PASSWORD) \
		-e $(LOG_PATH)/flowable:/tmp/data \
		-v $(LOG_PATH)/tomcat:/usr/local/tomcat/logs \
		flowable-all:v$(FLOWABLE_VERSION)
	docker logs -f $(CONTAINER_NAME)

rm:
	-docker rm -f $(CONTAINER_NAME)	
