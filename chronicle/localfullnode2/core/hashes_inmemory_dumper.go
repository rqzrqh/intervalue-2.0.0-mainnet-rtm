package core

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/localfullnode2/rpc"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/13/20 10:58 PM
 * @Description: doubtful whether worked well in massive messages case
 */

var (
	messagesHashes    []string
	sysMessagesHashes []string
)

type lazyMessageHashesHandler struct {
}

func (h lazyMessageHashesHandler) OnMessages(mHashes *rpc.StringArray) {
	for _, mHash := range mHashes.Data {
		log.Debug().Msgf("message hash: %s", mHash)
		messagesHashes = append(messagesHashes, mHash)

	}
}

func (h lazyMessageHashesHandler) OnError(e error) {
	log.Error().Msgf("error: %s", e)
}

func (h lazyMessageHashesHandler) OnFinished() {
	log.Info().Msgf("finished")
}

type lazySysMessageHashesHandler struct {
}

func (h lazySysMessageHashesHandler) OnMessages(mHashes *rpc.StringArray) {
	for _, mHash := range mHashes.Data {
		log.Debug().Msgf("system message hash: %s", mHash)
		sysMessagesHashes = append(sysMessagesHashes, mHash)

	}
}

func (h lazySysMessageHashesHandler) OnError(e error) {
	log.Error().Msgf("error: %s", e)
}

func (h lazySysMessageHashesHandler) OnFinished() {
	log.Info().Msgf("finished")
}

//collect all hashes(of message and system message) in memory slice
//retrieve wrapped message list by hashes
//meanwhile,dump list into chronicle
func (localfullnode2Chronicle *Localfullnode2Chronicle) HashesInMemoryDump() {
	defer localfullnode2Chronicle.IBlockMgr.Close()
	defer localfullnode2Chronicle.IChronicleDumperRestorerRPC.Close()

	const _accumulate int = 5

	localfullnode2Chronicle.GetStreamMessageHashes(lazyMessageHashesHandler{})
	localfullnode2Chronicle.GetStreamSysMessageHashes(lazySysMessageHashesHandler{})

	//after {@var messagesHashes} and {@var sysMessagesHashes} were filled with hashes

	if len(messagesHashes) > 0 {
		_lastOneIndex := len(messagesHashes) - 1
		hashesArray := []string{}

		for index, messageHash := range messagesHashes {
			if (index+1)%_accumulate == 0 || index == _lastOneIndex {
				hashesArray = append(hashesArray, messageHash)

				messages, err := localfullnode2Chronicle.GetMessageStreamBy(hashesArray)
				if err != nil {
					log.Error().Msgf("localfullnode2Chronicle.GetMessageStreamBy error:%s", err)
					return
				}

				localfullnode2Chronicle.AddBlockByTransactions(messages)
				hashesArray = hashesArray[0:0]
			} else {
				hashesArray = append(hashesArray, messageHash)
			}

		}
	}

	if len(sysMessagesHashes) > 0 {
		_lastOneIndex := len(sysMessagesHashes) - 1
		hashesArray := []string{}

		for index, messageHash := range sysMessagesHashes {
			if (index+1)%_accumulate == 0 || index == _lastOneIndex {
				hashesArray = append(hashesArray, messageHash)

				messages, err := localfullnode2Chronicle.GetMessageStreamBy(hashesArray)
				if err != nil {
					log.Error().Msgf("localfullnode2Chronicle.GetMessageStreamBy error:%s", err)
					return
				}

				localfullnode2Chronicle.AddBlockByTransactions(messages)
				hashesArray = hashesArray[0:0]
			} else {
				hashesArray = append(hashesArray, messageHash)
			}

		}
	}

	localfullnode2Chronicle.done()

}
