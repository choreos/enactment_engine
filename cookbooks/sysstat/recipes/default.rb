#
# Cookbook Name:: sysstat
# Recipe:: default
#
# Copyright 2011,2012 John Dewey
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

package "sysstat" do
  action :upgrade
end

service "sysstat" do
  supports :restart => true, :status => true

  action [ :enable, :start ]
end

if platform? %w{debian ubuntu}
  template node['sysstat']['settings'] do
    source "sysstat.erb"
    owner  "root"
    group  "root"
    mode   0644

    variables(
      :enabled => node['sysstat']['enabled']
    )

    notifies :restart, resources(:service => "sysstat")

    ##
    # Not sure I agree with foodcritic here:
    # http://acrmp.github.com/foodcritic/#FC023

    #only_if { platform? %w{debian ubuntu} }
  end
end
