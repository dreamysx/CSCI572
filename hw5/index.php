<?php
ini_set('memory_limit','2048M');
include 'SpellCorrector.php';
include 'simple_html_dom.php';
header('Content-Type: text/html; charset=utf-8');
$div=false;
$correct = "";
$correct1="";
$output = "";
$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query)
{
  $choice = isset($_REQUEST['sort'])? $_REQUEST['sort'] : "default";
  require_once('Apache/Solr/Service.php');
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');

  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }
  try
  {
    if($choice == "default")
      $parameter=array('sort' => '');
    else{
      $parameter=array('sort' => 'pageRankFile desc');
    }
    $word = explode(" ",$query);
    $spell = $word[sizeof($word)-1];
    for($i=0;$i<sizeOf($word);$i++){
      ini_set('memory_limit',-1);
      ini_set('max_execution_time', 300);
      $correction = SpellCorrector::correct($word[$i]);
      if($correct!="")
        $correct = $correct."+".trim($correction);
      else{
        $correct = trim($correction);
      }
        $correct1 = $correct1." ".trim($correction);
    }
    $correct1 = str_replace("+"," ",$correct);
    $div=false;
    if(strtolower($query)==strtolower($correct1)){
      $results = $solr->search($query, 0, $limit, $parameter);
    }
    else {
      $div =true;
      $results = $solr->search($query, 0, $limit, $parameter);
      $url = "http://localhost/572/index.php?q=$correct&sort=$choice";
      $output = "Did you mean: <a href='$url'>$correct1</a>";
    }

  }
  catch (Exception $e)
  {
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>
<html>
  <head>
    <title>CSCI 572 HW5 -- JING PENG</title>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <script src="http://code.jquery.com/jquery-1.10.2.js"></script>
    <script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
  </head>
  <body>
    <form  accept-charset="utf-8" method="get">
      <label for="q">Search:</label>
      <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
      <br><br>
      Algorithm used: 
      <input type="radio" name="sort" value="default" <?php if(isset($_REQUEST['sort']) && $choice == "default") { echo 'checked="checked"';} ?>>Lucene(Solr)
      <input type="radio" name="sort" value="pagerank" <?php if(isset($_REQUEST['sort']) && $choice == "pagerank") { echo 'checked="checked"';} ?>>PageRank
      <br>
      <input type="submit"/>
    </form>
    <script>
   $(function() {
     var URL_PREFIX = "http://localhost:8983/solr/myexample/suggest?q=";
     var URL_SUFFIX = "&wt=json&indent=true";
     var count=0;

     $("#q").autocomplete({
       source : function(request, response) {
         var correct="",before="";
         var query = $("#q").val().toLowerCase();
         var character_count = query.length - (query.match(/ /g) || []).length;
         var space =  query.lastIndexOf(' ');
         if(query.length-1>space && space!=-1){
          correct=query.substr(space+1);
          before = query.substr(0,space);
        }
        else{
          correct=query.substr(0); 
        }
        var URL = URL_PREFIX + correct+ URL_SUFFIX;
        console.log(URL);
        $.ajax({
         url : URL,

         success : function(data) {
          var tmp = data.suggest.suggest;

          console.log(tmp, correct);
          var tags = tmp[correct]['suggestions'];
          var results = [];
          for(var i = 0; i < tags.length; i++){
            if(before===""){
              results.push(tags[i]['term']);
            }else{
              results.push(before+" "+tags[i]['term']);
            }
          }
          console.log(results);
          response(results);
        },
        dataType : 'jsonp',
        jsonp : 'json.wrf'
      });
      },
      minLength : 1
    })
   });
 </script>
<?php
if ($div){
  echo $output;
}
$count =0;
$pre="";
// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);
?>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    <ol>
<?php

  foreach ($results->response->docs as $doc)
  {
    $title = $doc->title;
    $id = $doc->id;
    $or_id = $id;
 
    $key = str_replace("/Users/dreamysx/Documents/USC/572/hw/hw4/tools/solr-7.3.0/HTML_files/","",$id); 
 
    $description = $doc->og_description;
    $url = $doc->og_url;

    $searchterm = $_GET["q"];
    $ar = explode(" ", $searchterm);
    // $html_to_text_files_dir = "/Users/dreamysx/Documents/USC/572/hw/hw4/tools/solr-7.3.0/HTML_files/";
    $filename = $id;
    $html = file_get_html($filename)->plaintext;
    $sentences = explode(".", $html);
    $words = explode(" ", $query);
    $snippet = "";
    $text = "/";
    $start_delim="(?=.*?\b";
    $end_delim=".*?)";
    // echo $query;
    foreach($words as $item){
      $text=$text.$start_delim.$item.$end_delim;
    }
    $text=$text."^.*$/i";
    // echo $text."\n";
    foreach($sentences as $sentence){
      // $sentence=strip_tags($sentence);
      // if (strstr(strtolower($sentence), $query)){
      //   echo "chuxian";
      // }
      // echo preg_match($text, "cbciw Telsa cnwocece");
      // strstr(strtolower($sentence), $query)
      if (preg_match($text, $sentence)){
        // echo $sentence;
        $snippet = $snippet.$sentence;
        foreach (explode(" ", $query) as $single_query) {
          $snippet=str_replace($single_query,"<b>".$single_query."</b>", $snippet);
          $snippet=str_replace(ucfirst($single_query),"<b>".ucfirst($single_query)."</b>", $snippet);
          $snippet=str_replace(strtoupper($single_query),"<b>".strtoupper($single_query)."</b>", $snippet);
        }

        if(strlen($snippet)>=160) 
            break;
        // echo $snippet;
        // if (preg_match("(&gt|&lt|\/|{|}|[|]|\|\%|>|<|:)",$sentence)>0){
        //   continue;
        // }
        // else{
        //   $snippet = $snippet.$sentence;
        //   if(strlen($snippet)>=160) 
        //     break;
        // }
      } 
      if($snippet == ""){
        $cur_query="";
        if (count($words) > 1) {
          foreach($words as $item) {
            $cur_query=$item;
            $singletext="/";
            $singletext=$singletext.$start_delim.$item.$end_delim."^.*$/i";
            // echo $singletext;
            if(preg_match($singletext, $sentence)){
              $snippet = $sentence;
              // $snippet=str_replace($query,"<b>".$query."</b>", $snippet);
              // $snippet=str_replace(ucfirst($query),"<b>".ucfirst($query)."</b>", $snippet);
              break;
            }
          }
        }
        if($snippet != ""){
          // $snip_arr=explode($cur_query, $snippet);
          // $new_snip="";

          // foreach ($snip_arr as $temp_word) {
          //   echo $temp_word;
          //   if($temp_word!=$cur_query){
          //     $new_snip=$new_snip.$temp_word;
          //   }
          //   if($temp_word==$cur_query){
          //     echo $temp_word;
          //     $new_snip=$new_snip."<b>".$temp_word."</b>";
          //   }
          // }
          // $snippet=$new_snip;
          $snippet=str_replace($cur_query,"<b>".$cur_query."</b>", $snippet);
          $snippet=str_replace(ucfirst($cur_query),"<b>".ucfirst($cur_query)."</b>", $snippet);
          $snippet=str_replace(strtoupper($cur_query),"<b>".strtoupper($cur_query)."</b>", $snippet);
          if(strlen($snippet)>160){
            $snippet=substr($snippet, 0, 75)." <b>".$cur_query."</b> ".substr($snippet, -80, -1);
          }
          // echo $new;
          break;
        }
      }
    }
    if($snippet == ""){
      $snippet = "No snippet found";
    }
?>

      <li>
            <b><a href="<?php echo $url ?>"><?php echo htmlspecialchars($doc->title, ENT_NOQUOTES, 'utf-8'); ?> </a></b><br>
            <i><a href="<?php echo $url ?>"><?php echo $url ?></a></i><br>
            <?php echo htmlspecialchars($id, ENT_NOQUOTES, 'utf-8'); ?><br>
            <?php 
            if($snippet == "No snippet found"){
              echo htmlspecialchars($snippet, ENT_NOQUOTES, 'utf-8');
            }else{
              echo "...".$snippet."...";
            }
            ?>
      </li><br>

<?php
  }
?>
    </ol>
<?php
}
?>
  </body>
</html>