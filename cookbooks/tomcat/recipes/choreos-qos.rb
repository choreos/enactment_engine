#
# Cookbook Name:: tomcat 
# Recipe:: choreos
#
# Deploys Tomcat with several useful libs
#
# Copyright 2012, USP
#
# Leonardo Leite, Eduardo Hideo, Carlos Eduardo M. Santos

include_recipe "tomcat::default"

template "/etc/tomcat6/server.xml" do
  source "server-choreos.xml.erb"
  owner "root"
  group "root"
  mode "0644"
  variables({
     :glimpseHost => "81.200.35.154"
  })
  notifies :restart, resources(:service => "tomcat")
end
