/*******************************************************************************
 * Copyright (c) 2013, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.client" );

rwt.client.ClientMessages = function() {
  this._messages = {
    "ServerError" : "服务端错误",
    "ServerErrorDescription" : "服务请求失败，请重试。",
    "ConnectionError" : "网络错误",
    "ConnectionErrorDescription" : "服务请求失败，请重试。",
    "SessionTimeout" : "会话过期",
    "SessionTimeoutDescription" : "用户长时间未操作，会话已过期。",
    "ClientError" : "客户端请求错误",
    "ClientErrorDescription" : "用户长时间未操作，会话已过期。",
    "Retry" : "重试",
    "Restart" : "重新登录",
    "Details" : "查看明细"
  };
};

rwt.client.ClientMessages.getInstance = function() {
  return rwt.runtime.Singletons.get( rwt.client.ClientMessages );
};

rwt.client.ClientMessages.prototype = {

  setMessages : function( messages ) {
    for( var id in messages ) {
      this._messages[ id ] = messages[ id ];
    }
  },

  getMessage : function( id ) {
    return this._messages[ id ] ? this._messages[ id ] : "";
  }

};
