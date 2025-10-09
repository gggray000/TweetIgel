import{API} from "./Context.js";
import {useContext, useEffect, useState} from "react";
import Post from "./Post.jsx"
import CommentSection from "./CommentSection.jsx";

function TweetFeedWithPagination({username, viewingUsername, setViewingUsername, setView, hashtag, setHashtag, changed, setChanged, searchTerm, type}){
    const api = useContext(API)
    const [feed, setFeed] = useState([])
    const [pageNum, setPageNum] = useState(0)
    const [maxPageNum, setMaxPageNum] = useState(1)

    var fetchListEndpoint;
    var fetchCountEndpoint;
    if(type === "feed" || type === "profile"){
        fetchListEndpoint = api + "/posts" + (viewingUsername===null ?"" :"/" + viewingUsername.toString()) + "?";
        fetchCountEndpoint = api + "/postsCount" + (viewingUsername===null ?"" :"/" + viewingUsername.toString())
    } else if (type === "hashtag") {
        fetchListEndpoint = api + "/hashtag/" + hashtag + "?";
        fetchCountEndpoint = api + "/hashtag/" + hashtag + "/postsCount";
    } else if (type === "search") {
        fetchListEndpoint = api + "/post/search?term=" + searchTerm + "&";
        fetchCountEndpoint = api + "/post/search/postCount?term=" + searchTerm;
    }

    useEffect(() => {

        fetch(fetchListEndpoint + "page=" + pageNum.toString(), {
            method:"GET",
            credentials: 'include'
        }).then(response => {
            if(!response.ok){
                alert("Unable to load posts list: " + response.statusText)
                return []
            }else{
                return response.json()
            }
        }).then(parsedResponse => {
            setFeed(parsedResponse)
        }).then(() => {
                fetch(fetchCountEndpoint, {
                    method:"GET",
                    credentials: 'include'
                }).then(response => {
                    if(!response.ok){
                        alert("Unable to get post count: " + response.statusText)
                    } else {
                        return response.json()
                    }
                }).then(count => {
                    setMaxPageNum(Math.max(0, Math.ceil(count / 20) - 1));
                    console.log("Post counts: " + count)
                    console.log("Max pages: " + maxPageNum);
                })
        })
    },[api, changed, pageNum, viewingUsername, hashtag, searchTerm])

    function lastPage(){
        if(pageNum > 0){
            setPageNum(pageNum-1)
        }
        else{
            alert("Already at the first page.")
        }
    }

    function nextPage(){
        if(pageNum < maxPageNum){
            setPageNum(pageNum + 1)
        }
        else{
            alert("Already at the last page.")
        }
    }

    function goBack(){
        setView("loggedIn")
        setViewingUsername(null)
    }

    return <>
        {type === "hashtag" || type === "search" ? <>
                <div>
                    <button className="pico-background-azure-450" onClick={goBack}>Back to Feed</button>
                    <br/>
                    <br/>
                </div>
            </>
            : <></>
        }
        {feed.length === 0? <>
                <h5>No post to display.</h5>
            </>
            : <>
                {type === "hashtag"? <>
                        <h3>#{hashtag}</h3>
                        <hr /></>
                    :<></>}
                <div role="group">
                    <button className="outline" onClick={lastPage}>❮ Last Page</button>
                    <button className="outline" onClick={nextPage}>Next Page ❯</button>
                </div>
                <kbd className="outline"><small> Page {pageNum + 1} of {maxPageNum + 1}</small></kbd>
                <ul>
                    {feed.map(post => (
                        <Post key={post.id} post={post} setView={setView} username={username}
                              changed={changed} setChanged={setChanged}
                              setViewingUsername={setViewingUsername} setHashtag={setHashtag}
                              feed={feed} setFeed={setFeed} >
                        </Post>)
                    )}
                </ul>
            </>}
    </>
}

export default TweetFeedWithPagination