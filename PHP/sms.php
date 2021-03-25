<?php
//Database Configuration

$host = "localhost";
$db_user = "root";
$db_password = "";
$db_name = "";

if(isset($_GET['pass'])){
if($_GET['pass']=='netstech'){

$connection = mysqli_connect($host,$db_user,$db_password,$db_name);

        header("Content-Type: application/json; charset=utf-8");
        $data="[";
        $qs=mysqli_query($connection,"select * from sms where status=0");
    	if(mysqli_num_rows($qs)>0){
		while($obj=mysqli_fetch_object($qs)){

			$data.="{'to':'".$obj->sender_phone."','message':'$obj->message'},";
				mysqli_query($connection,"update sms set status=1 where id='$obj->id'");
				$data=substr($data, 0, -1);
				}
			}
		}
		echo $data.="]";
	}
?>
