/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.framework.tn5250;

import java.util.Vector;

public class DataStreamQueue {

   private final Object lock = new Object();
   private final Vector vector;
   public DataStreamQueue () {
      vector = new Vector();
   }

   /**
    * @todo redo the throttling of large queues that are backed up
    *       This is the cause of numerous painting bugs.
    * @return a datastream object from queue
    * @throws InterruptedException
    */
   public Object get() throws InterruptedException {
      synchronized (lock) {
         // wait until there is something to read
         while (isEmpty()) {
            lock.wait();
         }

         /**
          * @todo here is the throttling code to look at
          *
          * just something here to try.  OK it works but we need to be a little
          *     more intelligent with the throttling.
          */
         if (vector.size() >= 20) {
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
            vector.remove(0);
//            System.out.println(vector.size());
         }
            // we have the lock and state we're seeking
         return vector.remove(0);
      }
   }

   public boolean isEmpty() {

      return vector.isEmpty();
   }

   public void clear() {

      synchronized (lock) {
         vector.clear();
         lock.notifyAll();
      }

   }

   public void put(Object o) {
      synchronized (lock) {
         vector.addElement(o);
//         if (vector.size() > 5)
//            System.out.println(vector.size());
         // tell waiting threads to wake up
         lock.notifyAll();
      }
   }
}