import React from 'react'

import { useAtomValue, useUpdateAtom } from 'jotai/utils'
import styled from 'styled-components'

import {
  addTopicUpdateAtom,
  topicSearchResultsAtom,
  topicSearchTextAtom,
  topicSearchTextForApiAtom,
} from '@/atom/topic'
import { useSearchTopics } from '@/hook/react-query/useSearchTopics'
import { addTopic as addTopicServer, TopicInfo } from '@/server/topic'
import { HOVER_BLUE } from '@/styles/classNames'

type Props = {}

const isContainTopic = (topics: TopicInfo[], topicName: string): boolean => {
  return topics.map((topic) => topic.name).includes(topicName)
}

const TopicSearchModal: React.FC<Props> = ({}) => {
  const text = useAtomValue(topicSearchTextAtom)
  const topicSearchResults = useAtomValue(topicSearchResultsAtom)
  const addTopic = useUpdateAtom(addTopicUpdateAtom)
  const textForApi = useAtomValue(topicSearchTextForApiAtom)
  const { searchTopics } = useSearchTopics(textForApi)

  const topicSearchExist = topicSearchResults?.length !== 0

  const onClickAddTopic = async (topicName: string) => {
    const res = await addTopicServer(topicName)
    if (res.success) {
      if (res.data) addTopic(res.data)
    }
  }

  const onClickSelectTopic = (topic: TopicInfo) => {
    if (topic) addTopic(topic)
  }

  if (!topicSearchExist && !text) return null

  return (
    <Container>
      {searchTopics.map((searchTopic) => (
        <div
          className={`cursor-pointer w-full p-4 text-link ${HOVER_BLUE}`}
          key={`topicSearchResult_${searchTopic.topicId}`}
          onClick={() => onClickSelectTopic(searchTopic)}>
          <p className={'text-ellipsis overflow-hidden whitespace-nowrap'}>{searchTopic.name}</p>
        </div>
      ))}
      {text && !isContainTopic(searchTopics, text) && (
        <div className={`cursor-pointer w-full p-4 text-link ${HOVER_BLUE}`} onClick={() => onClickAddTopic(text)}>
          {`"${text}" 을 추가합니다.`}
        </div>
      )}
    </Container>
  )
}

const Container = styled.div`
  width: 285px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  padding: 16px;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.25);
  background-color: #fff;
`

export default TopicSearchModal
