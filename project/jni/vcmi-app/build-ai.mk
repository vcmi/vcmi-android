LOCAL_DIR := $(call my-dir)

FILTER_OUT = $(foreach v,$2,$(if $(findstring $1,$v),,$v))

define vcmiwalk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call vcmiwalk, $(e)))
endef

include $(LOCAL_DIR)/ai-empty.mk
include $(LOCAL_DIR)/ai-stupid.mk
include $(LOCAL_DIR)/ai-battle.mk
include $(LOCAL_DIR)/ai-vcai.mk