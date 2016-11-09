LOCAL_DIR := $(call my-dir)
MY_PATH := $(LOCAL_DIR)
VCMI_DIR := $(MY_PATH)/vcmi-app

define vcmiwalk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call vcmiwalk, $(e)))
endef

include $(MY_PATH)/fuzzylite.mk
include $(MY_PATH)/minizip.mk