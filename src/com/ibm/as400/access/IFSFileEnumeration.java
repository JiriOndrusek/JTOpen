///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileEnumeration.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;


class IFSFileEnumeration
implements Enumeration
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // The block size is hardcoded based on the value chosen by OpNav.  
    // For now, it is not configurable.
    private static final int MAXIMUM_GET_COUNT_ = 128;



    private IFSFile[]       contents_;
    private IFSFile[]       contentsPending_ = new IFSFile[0];  // @A1a
    private boolean         contentsArePending_;  // @A1a - replaces 'done_'
    private IFSFile         file_;
    private IFSFileFilter   filter_;
    private int             index_;
    private String          pattern_;
    private String          restartName_;



    IFSFileEnumeration(IFSFile file, IFSFileFilter filter, String pattern)
    throws AS400SecurityException, IOException
    {
        file_ = file;
        filter_ = filter;
        pattern_ = pattern;
        nextBlock();  // Prime the pump.
        nextBlock();  // Move pending-contents into current-contents.   @A1a
    }


    public boolean hasMoreElements()
    {
        return (contentsArePending_ || (index_ < contents_.length));  // @A1c
    }


    private void nextBlock()
    throws AS400SecurityException, IOException
    {           
/* @A1d
        contents_ = file_.listFiles0(filter_, pattern_, MAXIMUM_GET_COUNT_, restartName_);
        index_ = 0;
        done_ = (contents_.length < MAXIMUM_GET_COUNT_);
        if (contents_.length > 0)
            restartName_ = contents_[contents_.length - 1].getName();
        else
            restartName_ = null;
*/

// @A1a
        contents_ = contentsPending_;
        index_ = 0;
        if (contentsPending_.length != 0) {
          contentsPending_ = file_.listFiles0(filter_, pattern_, MAXIMUM_GET_COUNT_, restartName_);
        }
        contentsArePending_ = (contentsPending_.length != 0);
        if (contentsArePending_)
            restartName_ = contentsPending_[contentsPending_.length - 1].getName();
        else
            restartName_ = null;
        // Note: The use of contents_ and contentsPending_ allows us to "look ahead" and detect end-of-list where the number of files on the system is an exact multiple of MAXIMUM_GET_COUNT_.
    }


    public Object nextElement()
    {
        if (index_ < contents_.length)
            return contents_[index_++];
        else if (done_)
            throw new NoSuchElementException();
        else {
            try {
                nextBlock();
            }
            catch(Exception e) {
                throw new NoSuchElementException();
            }
            return contents_[index_++];
        }
    }


}

