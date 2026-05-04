/**
 * Copyright © 2026, m2n GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of the University of Washington nor the names
 *       of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL UNIVERSITY OF
 * WASHINGTON BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#include "edu_uw_apl_commons_tsk4j_pool_Pool.h"

#include <tsk/libtsk.h>

/**
 * @author Bernhard Stiftner
 *
 * Various support routines plus implementations of pool.Pool
 * native methods.
 */

/*
 * Class:     edu_uw_apl_commons_tsk4j_pool_Pool
 * Method:    openImage
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL 
Java_edu_uw_apl_commons_tsk4j_pool_Pool_openImage
(JNIEnv *env, jobject thiz, jlong imgNativePtr, jlong offset ) {

  TSK_IMG_INFO* imgInfo = (TSK_IMG_INFO*)imgNativePtr;
  const TSK_POOL_INFO* poolInfo = tsk_pool_open_img_sing( imgInfo, offset, TSK_POOL_TYPE_DETECT );
  return (jlong)poolInfo;
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_pool_Pool
 * Method:    openPartition
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL 
Java_edu_uw_apl_commons_tsk4j_pool_Pool_openPartition
( JNIEnv *env, jobject thiz, jlong partitionNativePtr ) {

  TSK_VS_PART_INFO* partInfo = (TSK_VS_PART_INFO*)partitionNativePtr;
  const TSK_POOL_INFO* poolInfo = tsk_pool_open_sing( partInfo, TSK_POOL_TYPE_DETECT );
  return (jlong)poolInfo;
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_pool_Pool
 * Method:    close
 * Signature: (J)V
 */
JNIEXPORT void JNICALL 
Java_edu_uw_apl_commons_tsk4j_pool_Pool_close
(JNIEnv * env, jobject thiz, jlong nativePtr ) {
  
  TSK_POOL_INFO* info = (TSK_POOL_INFO*)nativePtr;
  tsk_pool_close( info );
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_pool_Pool
 * Method:    type
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_edu_uw_apl_commons_tsk4j_pool_Pool_type
(JNIEnv *env, jobject thiz, jlong nativePtr ) {

  TSK_POOL_INFO* info = (TSK_POOL_INFO*)nativePtr;
  return (jint)info->ctype;
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_pool_Pool
 * Method:    type2Name
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_edu_uw_apl_commons_tsk4j_pool_Pool_type2Name
(JNIEnv *env, jclass clazz, jint type ) {

  const char* cp = tsk_pool_type_toname( type );
  return cp == NULL ? NULL : (*env)->NewStringUTF( env, cp );
}

// eof

