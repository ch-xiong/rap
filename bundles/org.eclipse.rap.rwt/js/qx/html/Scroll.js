/*******************************************************************************
 * Copyright: 2004, 2012 1&1 Internet AG, Germany, http://www.1und1.de,
 *                       and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Rich Ajax Platform
 ******************************************************************************/

qx.Class.define("qx.html.Scroll",
{
  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {

    /**
     * Disables browser-native scrolling
     *
     * @type static
     * @param el {Element} html-element
     * @return {void}
     */
    disableScrolling : function(el)
    {
      this.enableScrolling(el);
      el.scrollLeft = 0;
      el.scrollTop = 0;
      qx.html.EventRegistration.addEventListener(el, "scroll", this._onscroll);
    },


    /**
     * Re-enables browser-native scrolling
     *
     * @type static
     * @param el {Element} html-element
     * @return {void}
     */
    enableScrolling : function( el ) {
      qx.html.EventRegistration.removeEventListener( el, "scroll", this._onscroll );
    },


    /**
     * Handler for the scroll-event
     *
     * @type static
     * @param ev {event} scroll-event
     * @return {void}
     */
    _onscroll : function(ev)
    {
      // RAP [if] Fix for bug 288737: Scroll bars are broken in Opera 10
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=288737
      var el = null;
      if(ev.target) {
        el = (ev.target == ev.currentTarget) ? ev.target : null;
      } else if(ev.srcElement){
        el = ev.srcElement;
      }
      if(el && (ev.scrollLeft != 0 || ev.scrollTop != 0)) {
        el.scrollLeft = 0;
        el.scrollTop = 0;
      }
    }
  }
});
