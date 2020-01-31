/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.cluster.testfixture.internal.tomcat;

import org.apache.catalina.Engine;
import org.apache.catalina.ha.session.ClusterSessionListener;
import org.apache.catalina.ha.session.DeltaManager;
import org.apache.catalina.ha.session.JvmRouteBinderValve;
import org.apache.catalina.ha.session.JvmRouteSessionIDBinderListener;
import org.apache.catalina.ha.tcp.ReplicationValve;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.group.interceptors.MessageDispatch15Interceptor;
import org.apache.catalina.tribes.group.interceptors.TcpFailureDetector;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.apache.catalina.tribes.transport.nio.NioReceiver;
import org.apache.catalina.tribes.transport.nio.PooledParallelSender;
import org.eclipse.rap.rwt.cluster.testfixture.internal.util.SocketUtil;

// configure() was sadapted from the XML snippet below: 
//  <Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster" channelSendOptions="8">
//    <Manager className="org.apache.catalina.ha.session.DeltaManager"
//      expireSessionsOnShutdown="false"
//      notifyListenersOnReplication="true" />
//    <Channel className="org.apache.catalina.tribes.group.GroupChannel">
//     <Membership className="org.apache.catalina.tribes.membership.McastService"
//       address="228.0.0.4" port="45564" frequency="500" dropTime="3000" />
//     <Receiver className="org.apache.catalina.tribes.transport.nio.NioReceiver"
//       address="auto" port="4000" autoBind="100" selectorTimeout="5000"
//       maxThreads="6" />
//     <Sender className="org.apache.catalina.tribes.transport.ReplicationTransmitter">
//      <Transport className="org.apache.catalina.tribes.transport.nio.PooledParallelSender" />
//     </Sender>
//     <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpFailureDetector" />
//     <Interceptor className="org.apache.catalina.tribes.group.interceptors.MessageDispatch15Interceptor" />
//    </Channel>
//    <Valve className="org.apache.catalina.ha.tcp.ReplicationValve" filter="" />
//    <Valve className="org.apache.catalina.ha.session.JvmRouteBinderValve" />
//    <ClusterListener className="org.apache.catalina.ha.session.JvmRouteSessionIDBinderListener" />
//    <ClusterListener className="org.apache.catalina.ha.session.ClusterSessionListener" />
//  </Cluster>
class ClusterConfigurer {
  private static final String LOCALHOST = "127.0.0.1";
  private static final int CHANNEL_SEND_OPTIONS 
    = Channel.SEND_OPTIONS_DEFAULT | Channel.SEND_OPTIONS_SYNCHRONIZED_ACK;
  
  private final Engine engine;

  ClusterConfigurer( Engine engine ) {
    this.engine = engine;
  }
  
  void configure() {
    SimpleTcpCluster cluster = new SimpleTcpCluster();
    GroupChannel channel = new GroupChannel();
    cluster.setChannel( channel );
    configureCluster( cluster );
    configureChannel( channel );
    engine.setCluster( cluster );
  }

  private void configureChannel( GroupChannel channel ) {
    channel.setChannelReceiver( createChannelReceiver() );
    channel.setChannelSender( createChannelSender() );
    channel.addInterceptor( new TcpFailureDetector() );
    channel.addInterceptor( new MessageDispatch15Interceptor() );
  }

  private void configureCluster( SimpleTcpCluster cluster ) {
    cluster.setManagerTemplate( createDeltaManager() );
    cluster.setChannelSendOptions( CHANNEL_SEND_OPTIONS );
    cluster.addValve( new ReplicationValve() );
    cluster.addValve( new JvmRouteBinderValve() );
    cluster.addClusterListener( new JvmRouteSessionIDBinderListener() );
    cluster.addClusterListener( new ClusterSessionListener() );
  }

  private DeltaManager createDeltaManager() {
    DeltaManager result = new DeltaManager();
    result.setDistributable( true );
    result.setExpireSessionsOnShutdown( false );
    result.setNotifySessionListenersOnReplication( true );
    return result;
  }

  private NioReceiver createChannelReceiver() {
    NioReceiver result = new NioReceiver();
    result.setAddress( LOCALHOST );
    result.setPort( SocketUtil.getFreePort() );
    result.setAutoBind( -1 );
    result.setSelectorTimeout( 5000 );
    result.setMinThreads( 1 );
    return result;
  }

  private ReplicationTransmitter createChannelSender() {
    ReplicationTransmitter result = new ReplicationTransmitter();
    result.setTransport( new PooledParallelSender() );
    return result;
  }
}
