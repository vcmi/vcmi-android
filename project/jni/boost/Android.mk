LOCAL_PATH := $(call my-dir)
this_LOCAL_PATH := $(LOCAL_PATH)

BOOST_CPPFLAGS := -fexceptions -pthread
BOOST_CPPFLAGS += -DANDROID -D_REENTRANT -D_GLIBCXX__PTHREADS

define vcmiwalk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call vcmiwalk, $(e)))
endef

BOOST_ROOT := $(this_LOCAL_PATH)/boost_1_61_0

include $(this_LOCAL_PATH)/boost.mk

LOCAL_MODULE := boost-shared
LOCAL_PATH := $(this_LOCAL_PATH)
LOCAL_STATIC_LIBRARIES := boost-datetime boost-filesystem

# Other stuff: specify source/include files, set compilation flags, etc.

include $(BUILD_SHARED_LIBRARY)

