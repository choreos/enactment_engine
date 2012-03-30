#
# Cookbook Name:: generic-jar-service-template
# Recipe:: default
#
# Copyright 2012, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#

##########################################################################
#                  #
#                IMPORTANT DEVELOPMENT NOTICE:                           #
#                  #
# All ocurrences of $ NAME must be replaced with the actual service name #
#            before uploading the recipe to the chef-server              #
#                  #
##########################################################################

include_recipe "tomcat"

remote_file "jar_file" do
  source "#{node['service']['$NAME']['URL']}"
  path "#{node['service']['$NAME']['jarFiles']}/service$NAMEDeploy.jar"
  mode "0777"
  action :create
end

execute "execute_jar"
  command "java -jar service$NAMEDeploy.jar"
  action :run
  cwd "#{node['service']['$NAME']['jarFiles']}/"
end