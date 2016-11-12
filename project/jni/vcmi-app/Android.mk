LOCAL_DIR := $(call my-dir)

FILTER_OUT = $(foreach v,$2,$(if $(findstring $1,$v),,$v))

define vcmiwalk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call vcmiwalk, $(e)))
endef

include $(LOCAL_DIR)/build-lib.mk
include $(LOCAL_DIR)/build-ai.mk
include $(LOCAL_DIR)/build-client.mk